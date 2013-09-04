package example.paywall;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;
import com.polopoly.paywall.Subscription;
import com.polopoly.paywall.cookie.OnlineAccessCookie;
import com.polopoly.paywall.cookie.OnlineAccessDigestCookie;
import com.polopoly.user.server.User;

import example.content.article.StandardArticlePolicy;
import example.membership.UserDataHandler;
import example.membership.UserDataHandlerImpl;
import example.membership.UserHandler;
import example.membership.UserHandlerImpl;

/**
 * Servlet used for buying subscriptions to premium content.
 */
@SuppressWarnings("serial")
public class PaywallServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PaywallServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action");
        PolicyCMServer cmServer = RequestPreparator.getCMServer(req);

        UserHandler userHandler = new UserHandlerImpl();
        User user = userHandler.getUserIfPresent(req, resp);
        String sessionKey = userHandler.getSessionKeyIfPresent(req);

        if (user != null && sessionKey != null) {
            try {
                if ("success".equals(action)) {
                    buyProduct(req, resp, cmServer, user, sessionKey);
                }
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Error buying product", e);
            }
        } else {
            LOG.log(Level.WARNING, "Calling paywall servlet with invalid user and/or caller.");
        }

        doRedirect(req, resp, cmServer);
    }

    private void doRedirect(HttpServletRequest req, HttpServletResponse resp, PolicyCMServer cmServer) throws IOException {
        String articleIdStr = req.getParameter("articleId");
        String referrer = req.getParameter("referrer");
        String backUrl = "/";
        if (articleIdStr != null) {
            try {
                URLBuilder urlBuilder = RequestPreparator.getURLBuilder(req);
                ContentId articleId = ContentIdFactory.createContentId(articleIdStr);
                StandardArticlePolicy article;
                article = (StandardArticlePolicy) cmServer.getPolicy(articleId);
                backUrl = urlBuilder.createUrl(article.getParentIds(), req);
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Unable to create redirect url. Will redirect to /.", e);
            }
        } else if (!StringUtils.isEmpty(referrer)) {
            backUrl = referrer;
        }
        resp.sendRedirect(backUrl);
    }

    private void buyProduct(HttpServletRequest req, HttpServletResponse resp, PolicyCMServer cmServer, User user, String sessionKey) throws RemoteException, CMException {
        PaywallPolicy paywall = PaywallPolicy.getPaywallPolicy(cmServer);

        String subscriptionId = req.getParameter("subscriptionId");
        Subscription subscription = paywall.getSubscription(ContentIdFactory.createContentId(subscriptionId));

        UserDataHandler handler = new UserDataHandlerImpl(cmServer);
        ContentPolicy userDataPolicy = handler.getUserData(user.getUserId());

        PremiumUserDataPolicy writableUserDataPolicy = (PremiumUserDataPolicy) cmServer.createContentVersion(userDataPolicy.getContentId());
        try {
            writableUserDataPolicy.buySubscription(subscription);
            cmServer.commitContent(writableUserDataPolicy);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to buy product: " + subscription, e);
            cmServer.abortContent(writableUserDataPolicy);
        }

        Collection<ContentBundle> contentBundles = writableUserDataPolicy.getAccessibleContentBundlesByCapability(paywall.getOnlineAccessCapability(cmServer));
        OnlineAccessCookie onlineAccessCookie = new OnlineAccessCookie(contentBundles);
        resp.addCookie(onlineAccessCookie);

        OnlineAccessDigestCookie onlineAccessDigestCookie = new OnlineAccessDigestCookie(onlineAccessCookie, sessionKey, paywall.getSecret());
        resp.addCookie(onlineAccessDigestCookie);
    }
}
