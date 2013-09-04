package example.blog.command;

import com.polopoly.model.ModelPathUtil;

import example.blog.CmRenderContext;
import example.util.Command;
import example.util.Context;

public class BreakChainIfClosedForCommentsCommand implements Command
{
    public boolean execute(Context context)
    {
        CmRenderContext cmRenderContext = (CmRenderContext) context;

        Boolean open = (Boolean) ModelPathUtil.get(cmRenderContext.getLocalModel(), "content/openForComments");
        return (open == null || open);
    }
}
