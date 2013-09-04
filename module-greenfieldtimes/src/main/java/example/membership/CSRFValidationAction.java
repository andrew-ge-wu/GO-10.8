package example.membership;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Caller;

import example.util.CSRFUtil;

public class CSRFValidationAction
    implements Action
{
    private final CSRFUtil csrfUtil = new CSRFUtil();
    private final Action _wrapped;
    private final PolicyCMServer _cmServer;

    public CSRFValidationAction(PolicyCMServer cmServer, Action wrapped)
    {
        _cmServer = cmServer;
        _wrapped = wrapped;

    }

    public void perform(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {

        Caller caller = _cmServer.getCurrentCaller();

        if (csrfUtil.validate(caller.getSessionKey(), request)) {
            _wrapped.perform(request, response);
        }
    }
}
