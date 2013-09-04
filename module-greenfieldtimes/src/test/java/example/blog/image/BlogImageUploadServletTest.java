package example.blog.image;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.LockInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.JettyWrapper;
import example.MockitoBase;
import example.blog.BlogPolicy;
import example.blog.BlogPostPolicy;
import example.blog.image.command.AssertBlogContentIdCommand;
import example.blog.image.command.AssertMultipartContentRequestCommand;
import example.blog.image.command.AssertUserAllowedCommand;
import example.blog.image.command.EnsureBlogPostPresentCommand;
import example.blog.image.command.ImportBlogImageCommand;
import example.blog.image.command.MultipartRequestExtractorCommand;
import example.membership.UserHandler;
import example.util.ChainImpl;

public class BlogImageUploadServletTest extends MockitoBase {
    
    private JettyWrapper jetty;
    
    @Mock private CmClient cmClient;
    @Mock private PolicyCMServer cmServer;
    @Mock private UserHandler userHandler;
    
    @Mock User loggedInUser;
    @Mock UserId loggedInUserId;
    
    @Mock BlogPolicy blogPolicy;
    
    @Mock Content blogPostContent;
    @Mock BlogPostPolicy blogPostPolicy;
    
    @Mock ImageManagerPolicy imageManagerPolicy;
    
    @Mock LockInfo lockInfo;
    
    private TestBlogImageUploadServlet testServlet;

    public void testNotMultipart()
        throws Exception
    {
        HttpClient client = new HttpClient();
        
        PostMethod post = new PostMethod(jetty.getURL("/blogimageupload"));
        
        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.BAD_INPUT,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
    }
    
    public void testMultipartWithMissingBlogContentId()
        throws Exception
    {
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod(null, null, null);
        
        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.BAD_INPUT,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
    }
    
    public void testMultipartUserNotLoggedIn()
        throws Exception
    {
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(null);
        
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", null, null);
        
        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.PERMISSION_DENIED,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
    }
    
    public void testMultipartUserNotOwner()
        throws Exception
    {
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(ContentIdFactory.createContentId("19.100"))).thenReturn(blogPolicy);
        
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(false);

        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", null, null);
        
        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.PERMISSION_DENIED,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
        
        verify(userHandler, times(1)).getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null));
        verify(blogPolicy, times(1)).isAllowedToEdit(loggedInUserId);
    }
    
    public void testUploadBlogImageOnNewPost()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, 0);
        
        ContentId blogPostItExternalId = new ExternalContentId("example.BlogPost");
        
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        
        when(cmServer.createContent(19, blogContentId, blogPostItExternalId)).thenReturn(blogPostPolicy);
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);

        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenReturn("imagepath");

        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", null, createTempFileWithData(100));
        
        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.OK,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
        
        verify(cmServer, times(1)).createContent(19, blogContentId, blogPostItExternalId);
        verify(blogPostPolicy, times(1)).getContent();
        
        verify(cmServer, never()).createContentVersion(any(VersionedContentId.class));
        verify(cmServer, never()).getPolicy(not(eq(blogContentId)));
        
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostPolicy, times(1)).flushCache();
    }
    
    public void testUploadBlogImageOnExistingPost()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, VersionedContentId.LATEST_COMMITTED_VERSION);

        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        
        when(cmServer.createContentVersion(blogPostContentId)).thenReturn(blogPostPolicy);
        
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);
    
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenReturn("imagepath");
    
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", "19.101", createTempFileWithData(100));
        
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.OK,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);

        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, times(1)).getContent();
        
        verify(cmServer, times(1)).createContentVersion(eq(blogPostContentId));
        verify(cmServer, never()).getPolicy(not(eq(blogContentId)));
        
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostPolicy, times(1)).flushCache();
    }
    
    public void testUploadBlogImageOnPostInEdit()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, 0);
        
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(blogPostPolicy);
        
        when(cmServer.getLockInfo(blogPostContentId)).thenReturn(null);

        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);
    
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenReturn("imagepath");
    
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", "19.101.0", createTempFileWithData(100));
        
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.OK,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
        
        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, times(2)).getContent();
      
        verify(cmServer, never()).createContentVersion(eq(blogPostContentId));
        verify(cmServer, times(1)).getPolicy(not(eq(blogContentId)));
      
        verify(blogPostPolicy, times(1)).lock();
      
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostPolicy, times(1)).flushCache();
    }
    
    public void testUploadBlogImageOnPostInEditLocked()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, 0);
        
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(blogPostPolicy);
        
        when(cmServer.getLockInfo(blogPostContentId)).thenReturn(lockInfo);
    
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getLockInfo()).thenReturn(lockInfo);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);
    
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenReturn("imagepath");
    
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", "19.101.0", createTempFileWithData(100));
        
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.OK,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
        
        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, times(2)).getContent();
      
        verify(cmServer, never()).createContentVersion(eq(blogPostContentId));
        verify(cmServer, times(1)).getPolicy(not(eq(blogContentId)));
      
        verify(blogPostPolicy, never()).lock();
      
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostPolicy, times(1)).flushCache();
    }
    
    public void testTooLargeFileInBytes()
        throws Exception
    {
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", null, createTempFileWithData(2000000));
        
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
    }
    
    
    public void testCMExceptionInAssertUserAllowedCommand()
        throws Exception
    {
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(ContentIdFactory.createContentId("19.100"))).thenThrow(new CMException(""));

        HttpClient client = new HttpClient();

        PostMethod post = createMultipartPostMethod("19.100", null, null);

        int status = client.executeMethod(post);

        assertEquals(FckEditorUploadResponse.Status.SERVER_ERROR,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());

        assertEquals(HttpServletResponse.SC_OK, status);

        verify(userHandler, times(1)).getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null));
        verify(blogPolicy, never()).isAllowedToEdit(loggedInUserId);
    }
    
    public void testMalformedBlogContentId()
        throws Exception
    {
        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("malformedContentId", null, createTempFileWithData(100));
        
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.BAD_INPUT,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
        
        assertEquals(HttpServletResponse.SC_OK, status);
    }
    
    public void testCMExceptionInEnsureBlogPostPresentCommand()
      throws Exception
    {
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, VersionedContentId.LATEST_COMMITTED_VERSION);
          
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
          
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        when(cmServer.createContentVersion(any(VersionedContentId.class))).thenThrow(new CMException(""));

        HttpClient client = new HttpClient();
        
        PostMethod post = createMultipartPostMethod("19.100", "19.101", createTempFileWithData(100));
          
        int status = client.executeMethod(post);
        
        assertEquals(FckEditorUploadResponse.Status.SERVER_ERROR,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
          
        assertEquals(HttpServletResponse.SC_OK, status);
          
        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, never()).getContent();
        
        verify(cmServer, times(1)).createContentVersion(eq(blogPostContentId));
        verify(cmServer, never()).getPolicy(not(eq(blogContentId)));
        
        verify(cmServer, never()).getLockInfo(blogPostContentId);
        verify(blogPostPolicy, never()).lock();
       
        verify(imageManagerPolicy, never()).importImage(any(String.class), any(InputStream.class));
        verify(blogPostContent, never()).flushCache();
    }
    
    public void testCMExceptionInImportBlogImageCommand()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, 0);
      
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
      
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(blogPostPolicy);
      
        when(cmServer.getLockInfo(blogPostContentId)).thenReturn(lockInfo);
        when(blogPostContent.getLockInfo()).thenReturn(lockInfo);
    
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);
    
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenThrow(new CMException(""));
    
        HttpClient client = new HttpClient();
      
        PostMethod post = createMultipartPostMethod("19.100", "19.101.0", createTempFileWithData(100));
      
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.SERVER_ERROR,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
      
        assertEquals(HttpServletResponse.SC_OK, status);
      
        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, times(2)).getContent();
    
        verify(cmServer, never()).createContentVersion(eq(blogPostContentId));
        verify(cmServer, times(1)).getPolicy(not(eq(blogContentId)));
    
        verify(blogPostPolicy, never()).lock();
    
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostContent, never()).flushCache();
    }
    
    public void testImageTooBigInPixels()
        throws Exception
    {   
        ContentId blogContentId = ContentIdFactory.createContentId("19.100");
        VersionedContentId blogPostContentId = new VersionedContentId(19, 101, 0);
      
        when(userHandler.getLoggedInUser(isA(HttpServletRequest.class), (HttpServletResponse) eq(null))).thenReturn(loggedInUser);
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
      
        when(loggedInUser.getUserId()).thenReturn(loggedInUserId);
        when(blogPolicy.isAllowedToEdit(loggedInUserId)).thenReturn(true);
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(blogPostPolicy);
      
        when(cmServer.getLockInfo(blogPostContentId)).thenReturn(lockInfo);
        when(blogPostContent.getLockInfo()).thenReturn(lockInfo);
    
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(blogPostContentId);
    
        when(blogPostPolicy.getChildPolicy("images")).thenReturn(imageManagerPolicy);
        when(imageManagerPolicy.importImage(any(String.class), any(InputStream.class))).thenThrow(new ImageTooBigException(""));
    
        HttpClient client = new HttpClient();
      
        PostMethod post = createMultipartPostMethod("19.100", "19.101.0", createTempFileWithData(100));
      
        int status = client.executeMethod(post);
    
        assertEquals(FckEditorUploadResponse.Status.IMAGE_TOO_LARGE,
                     testServlet.testBlogImageContext.getFckEditorResponse().getStatus());
      
        assertEquals(HttpServletResponse.SC_OK, status);
      
        verify(cmServer, never()).createContent(eq(19), any(ContentId.class), any(ContentId.class));
        verify(blogPostPolicy, times(2)).getContent();
    
        verify(cmServer, never()).createContentVersion(eq(blogPostContentId));
        verify(cmServer, times(1)).getPolicy(not(eq(blogContentId)));
    
        verify(blogPostPolicy, never()).lock();
    
        verify(imageManagerPolicy, times(1)).importImage(any(String.class), any(InputStream.class));
        verify(blogPostContent, never()).flushCache();
    }

    private File createTempFileWithData(int byteCnt)
        throws Exception
    {
        String fileName = "" + System.currentTimeMillis();
        File tempFile = File.createTempFile(fileName, "data");
    
        OutputStream out = new FileOutputStream(tempFile);
        
        for (int i = 0; i < byteCnt; i++) {            
            out.write(i);
        }
        
        out.flush();
        out.close();
        
        return tempFile;
    }

    private PostMethod createMultipartPostMethod(String blogIdString, String blogPostIdString, File dataFile)
        throws FileNotFoundException
    {
        PostMethod filePost = new PostMethod(jetty.getURL("/blogimageupload"));
        
        List<Part> parts = new ArrayList<Part>();

        if (dataFile != null) {
            parts.add(new FilePart("blog_post_image", dataFile));
        }
        
        if (blogIdString != null) {
            parts.add(new StringPart("blogId", blogIdString));
        }
        
        if (blogPostIdString != null) {
            parts.add(new StringPart("blogPostId", blogPostIdString));
        }
        
        Part[] realParts = parts.toArray(new Part[0]);

        filePost.setRequestEntity(new MultipartRequestEntity(realParts, filePost.getParams()));
        
        return filePost;
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        jetty = new JettyWrapper();
        
        testServlet = new TestBlogImageUploadServlet();
   
        jetty.addServlet(testServlet, "/blogimageupload/*");
        jetty.start();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        jetty.stop();
        
        super.tearDown();
    }
    
    @SuppressWarnings("serial")
    private class TestBlogImageUploadServlet
        extends BlogImageUploadServletBase
    {
        @Override
        public void init(ServletConfig config)
            throws ServletException
        {
            super.init(config);
            
            _cmClient = cmClient;
            _cmServer = cmServer;
            _userHandler = userHandler;
            
            _communityMajor = 19;
            
            _chain = new ChainImpl();
            
            _chain.addCommand(new AssertMultipartContentRequestCommand());
            _chain.addCommand(new MultipartRequestExtractorCommand());
            _chain.addCommand(new AssertBlogContentIdCommand());
            _chain.addCommand(new AssertUserAllowedCommand());
            _chain.addCommand(new EnsureBlogPostPresentCommand());
            _chain.addCommand(new ImportBlogImageCommand());
        }

        public BlogImageRequestContext testBlogImageContext;
        
        @Override
        BlogImageContext createBlogImageContext(HttpServletRequest request)
        {
            testBlogImageContext = new BlogImageRequestContext(request,
                                                               userHandler,
                                                               cmServer,
                                                               19);
            
            return testBlogImageContext;
        }
    }
}
