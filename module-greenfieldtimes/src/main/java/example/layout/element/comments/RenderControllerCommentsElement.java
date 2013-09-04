package example.layout.element.comments;

import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.blog.AntiSamyHTMLValidator;
import example.blog.CmRenderContext;
import example.blog.command.BreakChainIfClosedForCommentsCommand;
import example.blog.command.BreakChainIfInvisibleCommentsCommand;
import example.blog.command.BreakChainUnlessHttpPostCommand;
import example.blog.command.CSRFValidationCommand;
import example.blog.command.PrepareCommentListCommand;
import example.comment.AddContentCommentCommand;
import example.content.BodyTranslator;
import example.content.RenderControllerExtended;
import example.membership.UserHandlerImpl;
import example.util.Chain;
import example.util.ChainImpl;
import example.util.RequestParameterUtil;

public class RenderControllerCommentsElement extends RenderControllerExtended
{
    public static final String HAS_COMMENT_ERROR = "hasCommentError";
    public static final String HAS_COMMENTS = "hasComments";

    private final Chain _viewChain;
    private final ChainImpl _postChain;

    public RenderControllerCommentsElement()
    {
        AntiSamyHTMLValidator htmlValidator = new AntiSamyHTMLValidator();

        _postChain = new ChainImpl();
        _postChain.addCommand(new BreakChainUnlessHttpPostCommand());
        _postChain.addCommand(new BreakChainIfClosedForCommentsCommand());
        _postChain.addCommand(new CSRFValidationCommand());
        _postChain.addCommand(new AddContentCommentCommand(htmlValidator));

        _viewChain = new ChainImpl();
        _viewChain.addCommand(new BreakChainIfInvisibleCommentsCommand());
        _viewChain.addCommand(new PrepareCommentListCommand());
    }

    @Override
    public void populateModelBeforeCacheKey(RenderRequest request, TopModel m,
            ControllerContext context)
    {
        super.populateModelBeforeCacheKey(request, m, context);

        ModelWrite localModel = m.getLocal();

        boolean inPreviewMode = m.getRequest().getPreview().isInPreviewMode();

        BodyTranslator bodyTranslator = new BodyTranslator();
        bodyTranslator.translateBody(request, localModel, "text", inPreviewMode);
    }

    public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo,
                                           ControllerContext context)
    {
        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        if (new RequestParameterUtil().isAjaxRequestMode(request)) {
            cacheInfo.setCacheTime(0);
        }

        CmRenderContext ctx = new CmRenderContext(request, m, context, getCmClient(context), new UserHandlerImpl());

        _postChain.execute(ctx);
        _viewChain.execute(ctx);
    }


}
