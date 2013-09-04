package example.blog.command;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.MockitoBase;
import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.membership.UserHandler;

public class PopulateBlogPostFieldDataCommandTest
    extends MockitoBase
{
    PopulateBlogPostFieldDataCommand toTest;
 
    @Mock BlogContext blogContext;
    @Mock PolicyCMServer cmServer;
    @Mock UserHandler userHandler;
    @Mock(extraInterfaces = { HttpServletRequest.class }) RenderRequest renderRequest;
    @Mock ModelWrite localModel;
    
    @Mock BlogPostPolicy blogPostPolicy;
    ContentId blogPostContentId = new ContentId(19, 200);
    
    @Mock User correctlyLoggedInUser;
    @Mock UserId correctlyLoggedInUserId;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new PopulateBlogPostFieldDataCommand();
    }
    
    public void testPopulateFieldData()
        throws Exception
    {
        when(blogContext.getLocalModel()).thenReturn(localModel);
        when(blogContext.getEditPostId()).thenReturn(blogPostContentId);
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(blogPostPolicy);
        
        when(blogPostPolicy.getName()).thenReturn("blog post name");
        when(blogPostPolicy.getText()).thenReturn("blog post text");

        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("Should return true.", doContinue);
        
        verify(localModel).setAttribute(eq("blogPostEchoName"), eq("blog post name"));
        verify(localModel).setAttribute(eq("blogPostEchoText"), eq("blog post text"));
    }
    
    public void testCMException()
        throws Exception
    {
        when(blogContext.getLocalModel()).thenReturn(localModel);
        when(blogContext.getEditPostId()).thenReturn(blogPostContentId);
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        
        when(cmServer.getPolicy(blogPostContentId)).thenThrow(new CMException(""));
    
        boolean doContinue = toTest.execute(blogContext);
        
        assertFalse("Should return false on error.", doContinue);
        
        verify(blogContext).addErrorShowBlogView("internalServerError");
        verify(localModel, never()).setAttribute(eq("blogPostEchoName"), eq("blog post name"));
        verify(localModel, never()).setAttribute(eq("blogPostEchoText"), eq("blog post text"));
        verify(localModel, never()).setAttribute(eq("blogPostId"), eq(blogPostContentId.getContentId().getContentIdString()));
    }
}
