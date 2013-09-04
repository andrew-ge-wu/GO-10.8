package example.blog.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.MockitoBase;
import example.blog.BlogContext;

public class AssertBlogPostBelongsToBlogCommandTest
    extends MockitoBase
{
    public AssertBlogPostBelongsToBlogCommand toTest;
    
    @Mock BlogContext blogContext;
    @Mock PolicyCMServer cmServer;
    @Mock ContentRead blogPostcontent;
    @Mock ModelWrite localModel;
    
    ContentId blogContentId = new ContentId(19, 100);
    ContentId blogPostContentId = new ContentId(19, 200);
    
    @Mock RenderRequest renderRequest;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new AssertBlogPostBelongsToBlogCommand();
    }
    
    public void testBlogPostBelongsToBlog()
        throws Exception
    {
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getBlogPostContentId()).thenReturn(blogPostContentId);
        
        when(cmServer.getContent(blogPostContentId)).thenReturn(blogPostcontent);
        when(blogPostcontent.getSecurityParentId()).thenReturn(blogContentId);
        
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("Returned false even when blog post belongs to blog.", doContinue);
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }
    
    public void testBlogPostDoNotBelongToBlog()
        throws Exception
    {
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getBlogPostContentId()).thenReturn(blogPostContentId);
        
        when(cmServer.getContent(blogPostContentId)).thenReturn(blogPostcontent);
        when(blogPostcontent.getSecurityParentId()).thenReturn(new ContentId(19, 500));
        
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertFalse("Returned true even when blog post didnt belong to blog.", doContinue);
        verify(blogContext).addErrorShowBlogView("permissionDenied");
    }
}
