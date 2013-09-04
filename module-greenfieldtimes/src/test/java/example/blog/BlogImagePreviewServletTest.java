package example.blog;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.polopoly.application.Application;
import com.polopoly.application.Hostname;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.ContentFileInfo;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.VersionInfo;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.path.ContentPathTranslator;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.management.ManagedBeanRegistry;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;

import example.JettyWrapper;
import example.MockitoBase;
import example.blog.image.BlogImagePreviewServlet;

public class BlogImagePreviewServletTest extends MockitoBase {


    private static final UserId USER_ID = new UserId("123");

    private static final ContentId ARBITRARY_CID = new ContentId(1,27);

    private static final ContentId BLOG_POST_CID = new ContentId(19,29);

    protected final static String PATHTRANSLATOR_ATTR = RequestPreparator.class.getName() + ".fcpt";

    private final ContentFileInfo fileInfo = new ContentFileInfo("", "foobar.txt", null, false, 0, "foobar".length());

    private JettyWrapper jetty;

    @Mock ContentPathTranslator pathTranslator;
    @Mock PolicyCMServer policyCmServer;
    @Mock CmClient cmClient;
    @Mock Content content;
    @Mock VersionInfo versionInfo;
    @Mock BlogPostPolicy blogPostPolicy;
    @Mock Application application;
    @Mock ManagedBeanRegistry registry;
    @Mock Hostname hostName;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        when(policyCmServer.getCurrentCaller()).thenReturn(new Caller(USER_ID,"fff",null));
        when(content.getFileInfo("foobar.txt")).thenReturn(fileInfo);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                OutputStream os = (OutputStream) invocation.getArguments()[1];
                os.write("foobar".getBytes("UTF-8"));
                return null;
            }
        }).when(content).exportFile(any(String.class), any(OutputStream.class));
        when(policyCmServer.getContent(BLOG_POST_CID.getLatestVersionId())).thenReturn(content);
        when(policyCmServer.getMajorByName(DefaultMajorNames.COMMUNITY)).thenReturn(19);
        when(application.getManagedBeanRegistry()).thenReturn(registry);
        when(application.getHostname()).thenReturn(hostName);
        when(application.getName()).thenReturn("application");
        when(application.getPreferredApplicationComponent(CmClient.class)).thenReturn(cmClient);
        when(cmClient.getPolicyCMServer()).thenReturn(policyCmServer);
        when(content.getVersionInfo()).thenReturn(versionInfo);
        when(versionInfo.isCommitted()).thenReturn(true);

        jetty = new JettyWrapper();

        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("p.applicationName", "application");
        jetty.getContext().setInitParams(initParams);
        ApplicationServletUtil.setApplication(jetty.getContext().getServletContext(), application);

        jetty.addServlet(new BlogImagePreviewServlet(),
                         "/preview/*",
                         new String[]{"hideServletExceptions", "true",
                                      "logExceptionsOnLevelFINE", "true"});

        jetty.addFilter(new RequestPreparatorReplacementFilter(), "/preview/*");
        jetty.start();

    }

    @Override
    protected void tearDown() throws Exception
    {
        jetty.stop();
        super.tearDown();
    }

    public void testGetIncorrectMajor()
        throws Exception
    {
        when(pathTranslator.getContentId("/1.27")).thenReturn(ARBITRARY_CID);

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(jetty.getURL("/preview/1.27!foobar.txt"));

        int responseCode = httpClient.executeMethod(getMethod);

        assertEquals("Unexpected response", HttpServletResponse.SC_NOT_FOUND, responseCode);

    }

    public void testCorrectMajorButNotOwner() throws Exception
    {
        when(pathTranslator.getContentId("/19.29")).thenReturn(BLOG_POST_CID);
        when(policyCmServer.getPolicy(BLOG_POST_CID.getLatestVersionId())).thenReturn(blogPostPolicy);
        when(blogPostPolicy.isAllowedToEdit(USER_ID)).thenReturn(false);

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(jetty.getURL("/preview/19.29!foobar.txt"));

        int responseCode = httpClient.executeMethod(getMethod);

        assertEquals("Unexpected response", HttpServletResponse.SC_FORBIDDEN, responseCode);

    }

    public void testCorrectMajorAndOwner() throws Exception
    {
        when(pathTranslator.getContentId("/19.29")).thenReturn(BLOG_POST_CID);
        when(policyCmServer.getPolicy(BLOG_POST_CID.getLatestVersionId())).thenReturn(blogPostPolicy);
        when(blogPostPolicy.isAllowedToEdit(USER_ID)).thenReturn(true);
        when(blogPostPolicy.getContent()).thenReturn(content);

        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(jetty.getURL("/preview/19.29!foobar.txt"));

        int responseCode = httpClient.executeMethod(getMethod);

        assertEquals("Unexpected response  '" + getMethod.getResponseBodyAsString() + "'", HttpServletResponse.SC_OK, responseCode);

        verify(content, times(1)).exportFile(eq("foobar.txt"), any(OutputStream.class));
    }

    class RequestPreparatorReplacementFilter implements Filter
    {

        private static final String APPLICATION_REQUEST_ATTR = "p.application";
        private ServletContext servletContext;

        public void destroy()
        {}

        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain filterChain) throws IOException, ServletException
        {
            request.setAttribute(PATHTRANSLATOR_ATTR, pathTranslator);
            request.setAttribute(APPLICATION_REQUEST_ATTR, application);
            filterChain.doFilter(request, response);
        }

        public void init(FilterConfig filterConfig) throws ServletException
        {
            servletContext = filterConfig.getServletContext();
            ApplicationServletUtil.setApplication(servletContext, application);
        }

    }
}