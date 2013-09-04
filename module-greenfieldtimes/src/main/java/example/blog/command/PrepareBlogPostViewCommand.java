package example.blog.command;

import com.polopoly.cm.ContentId;
import com.polopoly.model.ModelWrite;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.ContextScope;
import com.polopoly.siteengine.model.context.PageScope;
import com.polopoly.siteengine.model.request.ContentPath;

import example.blog.BlogContext;
import example.blog.RenderControllerBlog;
import example.util.Context;
import example.util.RequestParameterUtil;

public class PrepareBlogPostViewCommand extends BlogPostCommandBase
{
    public boolean execute(Context context)
    {
        BlogContext blogContext = (BlogContext) context;
        ModelWrite localModel = blogContext.getLocalModel();
        TopModel topModel = blogContext.getTopModel();
        ContextScope topContext = topModel.getContext();
        PageScope pageScope = topContext.getPage();
        
        if (new RequestParameterUtil().isAjaxRequestMode(blogContext.getRenderRequest())) {
            return true;
        }
        boolean mobileMode = isMobileMode(blogContext);

        ContentPath pathAfterPage = pageScope.getPathAfterPage();
        int pathSize = pathAfterPage.size();
        if (pathSize < 2) {
            return mobileMode;
        }
        ContentId blogPostId = pathAfterPage.get(1);
        localModel.setAttribute(RenderControllerBlog.BLOG_POST_ID, blogPostId);

        return mobileMode;
    }
}
