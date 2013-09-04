package example.paywall;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.cm.servlet.dispatcher.DispatcherPreparator;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.PaywallPolicy;

import example.content.article.StandardArticlePolicy;

/**
 * Filter for controlling the Paywall. Provides or denies access to premium content.
 */
public class PaywallFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(PaywallFilter.class.getName());
    private static final ExternalContentId PAYWALL_CONTENT_ID = new ExternalContentId("example.paywall");
    private static final String PAYWALL_ELEMENT = "example.PaywallElement";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = ((HttpServletRequest) request);
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        PolicyCMServer cmServer = DispatcherPreparator.getCMServer(httpRequest);

        try {
            PaywallPolicy paywall = PaywallPolicy.getPaywallPolicy(cmServer);

            if (paywall.isEnabled()) {

                Policy policy = getArticlePolicy(httpRequest, cmServer);

                if (policy != null && PAYWALL_ELEMENT.equals(policy.getInputTemplate().getName())) {
                    String requestedArticleIdStr = request.getParameter("aId");

                    if (requestedArticleIdStr != null && !"".equals(requestedArticleIdStr)) {
                        ContentId requestedArticleId = ContentIdFactory.createContentId(requestedArticleIdStr);
                        StandardArticlePolicy article = (StandardArticlePolicy) cmServer.getPolicy(requestedArticleId);
                        String articleUrl = buildArticleUrl(httpRequest, article);
                        if (paywall.isLocalAuthorizationEnabled()) {
                            if(paywall.hasOnlineAccess(httpRequest, article)) {
                                httpResponse.sendRedirect(articleUrl);
                                return;
                            }
                        } else {
                            httpResponse.setHeader("X-Premium-Redirect-Article-URL", articleUrl);
                            httpResponse.setHeader("X-Premium-Packages", getBundleIdsString(article));
                        }
                    }
                }

                if (policy instanceof StandardArticlePolicy) {
                    StandardArticlePolicy article = (StandardArticlePolicy) policy;

                    if (article.isPremiumContent()) {

                        boolean isWhitelistedUserAgent = paywall.hasWhitelistedUserAgent(httpRequest);
                        boolean isWhitelistedReferrer = paywall.hasWhitelistedReferrer(httpRequest);

                        String paywallUrl = buildPaywallUrl(httpRequest, article);

                        if (paywall.isLocalAuthorizationEnabled()) {

                            boolean hasOnlineAccess = paywall.hasOnlineAccess(httpRequest, article);

                            boolean hasMeteredAccess = false;
                            if (!hasOnlineAccess && paywall.isMetered()) {
                                hasMeteredAccess = paywall.hasMeteredAccess(httpRequest, article);
                                if (hasMeteredAccess) {
                                    paywall.updateMeteredWithViewedContent(httpRequest, httpResponse, article);
                                }
                            }

                            if (!(hasOnlineAccess || hasMeteredAccess || isWhitelistedUserAgent || isWhitelistedReferrer)) {
                                httpResponse.sendRedirect(paywallUrl);
                                return;
                            }
                        } else {
                            httpResponse.setHeader("X-Premium-Packages", getBundleIdsString(article));
                            httpResponse.setHeader("X-Premium-Redirect-URL", paywallUrl);
                            if (paywall.isMetered()) {
                                httpResponse.setHeader("X-Metered-Limit", String.valueOf(paywall.getMeteredNumberOfFreeClicks()));
                                httpResponse.setHeader("X-Metered-Period", String.valueOf(paywall.getMeteredPeriodInDays()));
                            }
                        }
                    }
                }
            }
        } catch (CMException e) {
            LOG.log(Level.FINE, "Error processing potential premium article", e);
        }

        chain.doFilter(request, response);
    }

    private String getBundleIdsString(StandardArticlePolicy article) throws CMException {
        HashSet<String> bundleIds = new HashSet<String>();
        Collection<? extends ContentBundle> premiumBundles = article.getPremiumBundles();
        for (ContentBundle bundle : premiumBundles) {
            bundleIds.add(bundle.getContentId().getContentId().getContentIdString());
        }
        return StringUtils.join(bundleIds.iterator(), ":");
    }

    @SuppressWarnings("serial")
    private String buildPaywallUrl(HttpServletRequest httpRequest, StandardArticlePolicy article) throws CMException {
        final String articleId = article.getContentId().getContentId().getContentIdString();
        URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
        ContentId[] articleParents = article.getParentIds();
        ContentId[] copyParents = Arrays.copyOf(articleParents, articleParents.length);
        copyParents[articleParents.length -1] = PAYWALL_CONTENT_ID;
        return urlBuilder.createUrl(copyParents, new HashMap<String, String>() {{put("aId", articleId);}}, httpRequest);
    }

    private String buildArticleUrl(HttpServletRequest httpRequest, StandardArticlePolicy article) throws CMException {
        URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
        return urlBuilder.createUrl(article.getParentIds(), httpRequest);
    }

    private Policy getArticlePolicy(HttpServletRequest httpRequest, PolicyCMServer cmServer) throws CMException {
        String requestURI = httpRequest.getRequestURI();
        int lastIndexOfSlash = requestURI.lastIndexOf("/");
        if (lastIndexOfSlash != -1) {
            int lastIndexOfDash = requestURI.lastIndexOf("-");
            lastIndexOfDash = lastIndexOfDash > -1 ? lastIndexOfDash : lastIndexOfSlash;
            if (lastIndexOfDash != -1) {
                String articleIdString = requestURI.substring(lastIndexOfDash + 1);
                try {
                    ContentId articleId = ContentIdFactory.createContentId(articleIdString);
                    return cmServer.getPolicy(articleId);
                } catch (IllegalArgumentException e) {
                    LOG.log(Level.FINE, "Error getting article id from request", e);
                }
            }
        }

        return null;
    }

    @Override
    public void destroy() {
    }
}
