package example.blog.command;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;

import example.MockitoBase;
import example.blog.BlogContext;
import example.blog.BlogPostPolicy;

public class DeleteBlogPostCommandTest extends MockitoBase{
    
    DeleteBlogPostCommand target;
    
    @Mock BlogContext blogContext;

    private ContentId contentId;

    @Mock PolicyCMServer cmServer;

    @Mock BlogPostPolicy blogPostPolicy;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    
        contentId = new ContentId(1,100);
        target = new DeleteBlogPostCommand();
        when(blogContext.getBlogPostContentId()).thenReturn(contentId);
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
    }
    
    public void testShouldDeleteBlogPostWithGivenPostId() throws Exception
    {
        when(cmServer.createContentVersion(contentId.getLatestCommittedVersionId()))
            .thenReturn(blogPostPolicy);
        
        boolean result = target.execute(blogContext);
        assertTrue(result);
        verify(blogPostPolicy).delete();
        verify(cmServer).commitContent(blogPostPolicy);

    }
    
    public void testShouldAbortCommandChainIfCMExceptionOccurs() throws Exception    
    {
        when(cmServer.createContentVersion(contentId.getLatestCommittedVersionId()))
            .thenThrow(new CMException("exception"));
        boolean result = target.execute(blogContext);
        assertFalse(result);        
    }

    public void testShouldAbortCommandChainAndAbortContentIfCMExceptionOccursAndNewVersionCreated() throws Exception    
    {
        when(cmServer.createContentVersion(contentId.getLatestCommittedVersionId()))
            .thenReturn(blogPostPolicy);
   
        doThrow(new CMException("exception")).when(cmServer).commitContent(blogPostPolicy);
        boolean result = target.execute(blogContext);
        assertFalse(result);        
    }

}
