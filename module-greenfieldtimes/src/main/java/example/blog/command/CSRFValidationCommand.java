package example.blog.command;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.user.server.Caller;

import example.blog.CmRenderContext;
import example.util.CSRFUtil;
import example.util.Command;
import example.util.Context;

/**
 * Command that prevents Cross Site Request Forgery by validating a required token.
 */
public class CSRFValidationCommand
    implements Command
{
    private final CSRFUtil csrfUtil = new CSRFUtil();

    public boolean execute(Context context)
    {
        CmRenderContext cmRenderContext = (CmRenderContext) context;

        HttpServletRequest request = (HttpServletRequest) cmRenderContext.getRenderRequest();

        Caller caller = cmRenderContext.getPolicyCMServer().getCurrentCaller();

        return csrfUtil.validate(caller.getSessionKey(), request);
    }
}