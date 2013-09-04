package example.membership;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionFailOnNoReferer implements Action
{
    private Action wrapped;

    public ActionFailOnNoReferer(Action action) {
        wrapped = action;
    }
    
    public void perform(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
    {
        String referrer = request.getHeader(HTTP_HEADER_NAME_REFERRER);
        
        if (null == referrer) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Browser error, no referer set in request.");
            return;
        }
        
        wrapped.perform(request, response);
    }

}
