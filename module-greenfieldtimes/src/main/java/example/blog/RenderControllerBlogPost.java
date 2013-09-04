package example.blog;

import com.polopoly.cm.client.CmClient;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import example.blog.command.PrepareBlogPostViewCommand;
import example.blog.command.PrepareCommentListCommand;
import example.content.BodyTranslator;
import example.content.RenderControllerExtended;
import example.membership.UserHandlerImpl;
import example.util.Chain;
import example.util.ChainImpl;
import example.util.RequestParameterUtil;

public class RenderControllerBlogPost extends RenderControllerExtended {
    
    private final Chain _commandChain;
    
    public RenderControllerBlogPost()
    {
        _commandChain = new ChainImpl();
        _commandChain.addCommand(new PrepareBlogPostViewCommand());
        _commandChain.addCommand(new PrepareCommentListCommand());
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
        
        CmClient cmClient = getCmClient(context);
        BlogContext ctx = new BlogRequestContext(request, m, context, cmClient, new UserHandlerImpl());
        
        _commandChain.execute(ctx);
    }
}
