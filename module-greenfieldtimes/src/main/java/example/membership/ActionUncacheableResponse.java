package example.membership;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionUncacheableResponse implements Action {

    private Action wrapped;

    public ActionUncacheableResponse(Action action) {
        wrapped = action;
    }

    public void perform(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setHeader("Cache-Control", "private, no-store, no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Cache-Control", "max-age=0, s-max-age=0");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        wrapped.perform(request, response);
    }
}
