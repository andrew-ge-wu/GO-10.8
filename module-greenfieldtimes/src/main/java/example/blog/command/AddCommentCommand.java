package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.filter.state.ModerationState;
import com.polopoly.cm.client.filter.state.ModerationState.State;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.comment.CommentList;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;
import com.polopoly.util.StringUtil;

import example.blog.AntiSamyHTMLValidator;
import example.blog.CmRenderContext;
import example.blog.HTMLValidator;
import example.blog.InvalidHTMLException;
import example.blog.RenderControllerBlog;
import example.comment.CommentPolicy;
import example.filter.CaptchaValidationFilter;
import example.membership.ActionLogin;
import example.membership.UserDataHandler;
import example.membership.UserDataHandlerImpl;
import example.util.Command;
import example.util.Context;

public abstract class AddCommentCommand implements Command
{
    private static final String ANONYMOUS_USER_LOGIN_NAME = "anonymous";
    private static final Logger LOG = Logger.getLogger(AddCommentCommand.class.getName());
    private static final ContentId commentItExternalId = new ExternalContentId("example.Comment");

    private final HTMLValidator _validator;
    private UserId _anonymousUserId;

    public AddCommentCommand(AntiSamyHTMLValidator validator)
    {
        _validator = validator;
    }

    public boolean execute(Context context)
    {
        CmRenderContext cmContext = (CmRenderContext) context;

        if (!isPostingToThisContent(cmContext)) {
            return false;
        }

        RenderRequest renderRequest = cmContext.getRenderRequest();
        PolicyCMServer cmServer = cmContext.getPolicyCMServer();
        HttpServletRequest httpRequest = (HttpServletRequest) renderRequest;

        String commentAuthor = getCommentAuthor(renderRequest, cmContext, cmServer);
        String commentText = getCommentText(renderRequest);

        if (!validateInput(httpRequest, commentText, commentAuthor, cmContext)) {
            return falseWithPopulatedFields(cmContext, commentText, commentAuthor);
        }

        String authorIPAddress = httpRequest.getRemoteAddr();
        Caller currentCaller = cmServer.getCurrentCaller();
        boolean loggedInAsSiteUser =
            cmContext.getUserHandler().isLoggedInAsSiteUser(httpRequest);
        String authorUserId = getUserIdString(cmContext);
        try {
            if (Caller.NOBODY_CALLER.equals(currentCaller)) {
                cmServer.setCurrentCaller(new Caller(getAnonymousUserId(cmContext), null, null));
            } else {
                currentCaller = null;
            }
            int major = cmServer.getMajorByName(DefaultMajorNames.COMMUNITY);
            ContentId parentId = getCommentsIdPostParameter(cmContext);

            CommentPolicy comment = (CommentPolicy) cmServer
                .createContent(major, parentId, commentItExternalId);

            String name = getParentNameHumanReadable(cmServer, parentId);
            comment.setName("Comment on '" + name + "' by '" + commentAuthor + "'");
            comment.setAuthor(commentAuthor);
            comment.setText(commentText);
            comment.setAuthorIP(authorIPAddress);
            comment.setAuthorLoggedIn(String.valueOf(loggedInAsSiteUser));
            comment.setAuthorUserId(authorUserId);

            ModerationState.State state = (ModerationState.State) ModelPathUtil.get(cmContext.getLocalModel(), "content/initialModerationState");
            if (state != null) {
                comment.setModerationState(state);
            } else {
                comment.setModerationState(State.PUBLIC_PENDING);
            }

            cmServer.commitContent(comment);

            CommentList commentList = getCommentList(cmContext);
            commentList.addFirst(comment.getContentId());

            return true;
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Error while creating comment.", e);
            showError(cmContext, RenderControllerBlog.ADD_COMMENT_ERROR);
        } catch (ServiceNotAvailableException e) {
            LOG.log(Level.WARNING, "Error while creating comment.", e);
            showError(context, RenderControllerBlog.ADD_COMMENT_ERROR);
        } finally {
            if (currentCaller != null) {
                cmServer.setCurrentCaller(currentCaller);
            }
        }

        return falseWithPopulatedFields(cmContext, commentText, commentAuthor);
    }

    private String getParentNameHumanReadable(PolicyCMServer cmServer, ContentId parentId) throws CMException
    {
        ContentRead content;
        String result;
        ContentId nextId = parentId;
        do {
            content = cmServer.getContent(nextId);
            result = content.getName();
        } while  (nextId.getMajor() == 7 && (nextId = content.getSecurityParentId()) != null );

        return result;
    }

    private synchronized UserId getAnonymousUserId(CmRenderContext cmRenderContext) {
        if (_anonymousUserId == null) {
            try {
                User anonymousUser = cmRenderContext.getCmClient().
                    getUserServer().getUserByLoginName(ANONYMOUS_USER_LOGIN_NAME);
                _anonymousUserId = anonymousUser.getUserId();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to find user " + ANONYMOUS_USER_LOGIN_NAME, e);
            }
        }
        return _anonymousUserId;
    }

    protected abstract CommentList getCommentList(Context context) throws CMException;

    protected abstract void showError(Context context, String errorKey);

    protected abstract boolean isPostingToThisContent(CmRenderContext cmContext);

    protected ContentId getCommentsIdPostParameter(Context context)
    {
        return ((CmRenderContext) context).getContentIdFromRequest(RenderControllerBlog.COMMENTS_ID);
    }

    private boolean falseWithPopulatedFields(CmRenderContext cmRenderContext,
                                             String commentText,
                                             String commentAuthor)
    {
        ModelWrite stack = cmRenderContext.getTopModel().getStack();
        stack.setAttribute(RenderControllerBlog.COMMENT_ECHO_TEXT, commentText);
        stack.setAttribute(RenderControllerBlog.COMMENT_ECHO_AUTHOR, commentAuthor);
        return false;
    }

    private boolean validateInput(HttpServletRequest request,
                                  String commentText,
                                  String commentAuthor,
                                  CmRenderContext cmRenderContext)
    {
        boolean isValid = true;

        //Boolean validCaptcha = (Boolean) request.getAttribute(CaptchaValidationFilter.IS_VALID_CAPTCHA_ATTRIBUTE);
        Boolean validCaptcha = true;
        boolean isMobileMode = cmRenderContext.getControllerContext().getMode().equals("mobile");

        // We don't require captcha for mobile mode, so check this too.
        if (!isMobileMode && (validCaptcha == null || !validCaptcha.booleanValue())) {
            showError(cmRenderContext, RenderControllerBlog.COMMENT_BAD_CAPTCHA);
            isValid = false;
        }
        if (StringUtil.isEmpty(commentText)) {
            showError(cmRenderContext, RenderControllerBlog.COMMENT_TEXT_EMPTY);
            isValid = false;
        }
        if (StringUtil.isEmpty(commentAuthor)) {
            showError(cmRenderContext, RenderControllerBlog.COMMENT_AUTHOR_EMPTY);
            isValid = false;
        }

        return isValid;
    }

    private String getUserIdString(CmRenderContext cmContext)
    {
        UserId userId = cmContext.getPolicyCMServer().getCurrentCaller().getUserId();
        return userId != null ? userId.getPrincipalIdString() : "<Anonymous user>";
    }

    private String getCommentText(RenderRequest request)
    {
        try {
            return _validator.stripAllHTML(request.getParameter("comment_text"));
        } catch (InvalidHTMLException e) {
            LOG.log(Level.WARNING, "Error while creating comment.", e);
            return "";
        }
    }

    private String getCommentAuthor(RenderRequest request, CmRenderContext ctx, PolicyCMServer cmServer)
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            User user = ctx.getUserHandler().getUserIfPresent(httpRequest, null);

            if (ctx.getUserHandler().isLoggedInAsSiteUser(httpRequest)) {
                UserDataHandler handler = new UserDataHandlerImpl(cmServer);
                ContentPolicy userData = handler.getUserData(user.getUserId());

                return userData.getChildPolicy(ActionLogin.CHILD_POLICY_SCREEN_NAME).getComponent("value");
            }
            String author = request.getParameter("comment_author");
            if (author != null) {
                return _validator.stripAllHTML(author).trim();
            }
            return "";
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Couldn't get user name.", e);
            return "";
        }
    }
}
