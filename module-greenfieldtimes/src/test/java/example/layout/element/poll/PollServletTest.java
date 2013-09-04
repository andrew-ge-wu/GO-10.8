package example.layout.element.poll;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import example.MockitoBase;

public class PollServletTest extends MockitoBase
{
    private final static String ATTR_FORWARD = "forward";

    @Mock HttpServletRequest _request;

    private PollServlet _ps;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _ps = new PollServlet();
    }

    public void testGetForwardHandlesNull()
        throws Exception
    {
        when(_request.getParameter(ATTR_FORWARD)).thenReturn(null);
        assertEquals("", "/", _ps.getForward(_request));
    }

    public void testGetForwardHandlesEmptyString()
        throws Exception
    {
        when(_request.getParameter(ATTR_FORWARD)).thenReturn("");
        assertEquals("", "/", _ps.getForward(_request));
    }
    
    public void testGetForward()
        throws Exception
    {
        when(_request.getParameter(ATTR_FORWARD)).thenReturn("http://www.atex.com");
        assertEquals("", "/", _ps.getForward(_request));

        when(_request.getParameter(ATTR_FORWARD)).thenReturn("http://www.atex.com/");
        assertEquals("", "/", _ps.getForward(_request));

        when(_request.getParameter(ATTR_FORWARD)).thenReturn("http://www.atex.com/news");
        assertEquals("", "/news", _ps.getForward(_request));
        
        when(_request.getParameter(ATTR_FORWARD)).thenReturn("/news");
        assertEquals("", "/news", _ps.getForward(_request));
        
        when(_request.getParameter(ATTR_FORWARD)).thenReturn("news");
        assertEquals("", "/news", _ps.getForward(_request));
    }
}
