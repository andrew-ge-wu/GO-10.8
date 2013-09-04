package example.blog.command;

import com.polopoly.render.CacheInfo;
import com.polopoly.siteengine.model.TopModel;

import example.blog.BlogContext;
import example.util.Command;
import example.util.Context;

public class EnsureCacheDisabledCommand implements Command
{
    public boolean execute(Context context)
    {
        if (context instanceof BlogContext) {
            BlogContext blogContext = (BlogContext) context;
            TopModel topModel = blogContext.getTopModel();

            CacheInfo cacheInfo = topModel.getRequest().getCall().getCacheInfo();
            cacheInfo.setPrivate(true);
            cacheInfo.setCacheTime(0);
        }
        
        return true;
    }
}
