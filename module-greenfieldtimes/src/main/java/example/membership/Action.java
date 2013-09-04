package example.membership;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Action {

    /* HTTP header fields. */
    public static final String HTTP_HEADER_NAME_REFERRER = "Referer";

    void perform(HttpServletRequest request,
                 HttpServletResponse response)
        throws IOException, ServletException;

}