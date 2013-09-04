package example.blog.command;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.render.RenderRequest;

import example.blog.CmRenderContext;
import example.util.Command;
import example.util.Context;

public class BreakChainUnlessHttpPostCommand implements Command
{
    public static final String REQUEST_METHOD_POST = "POST";

    public boolean execute(Context context)
    {
        CmRenderContext cmRenderContext = (CmRenderContext) context;

        if (isPostMethod(cmRenderContext.getRenderRequest())) {
            return true;
        }
        
        return false;
    }

    private boolean isPostMethod(RenderRequest request)
    {
        return BreakChainUnlessHttpPostCommand.REQUEST_METHOD_POST.equals(((HttpServletRequest) request).getMethod());
    }

}
