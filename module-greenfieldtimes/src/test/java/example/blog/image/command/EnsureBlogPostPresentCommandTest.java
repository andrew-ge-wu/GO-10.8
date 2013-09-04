package example.blog.image.command;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;

import example.MockitoBase;
import example.blog.BlogPostPolicy;
import example.blog.image.BlogImageRequestContext;
import example.blog.image.FckEditorUploadResponse;

public class EnsureBlogPostPresentCommandTest
    extends MockitoBase
{
    private EnsureBlogPostPresentCommand toTest = null;
    
    @Mock private BlogImageRequestContext blogImageContext;
    @Mock private HttpServletRequest request;
    @Mock private PolicyCMServer cmServer;
    
    @Mock FileItem blogIdFileItem;
    @Mock FileItem blogPostIdFileItem;
    @Mock Map<String, FileItem> fileItemMap;
    
    @Mock private BlogPostPolicy blogPostPolicy;
    @Mock Content blogPostContent;
    
    @Mock FckEditorUploadResponse fckEditorUploadResponse;
    
    ContentId blogContentId = new ContentId(19, 99);
    ContentId blogPostContentId = new ContentId(19, 100);
    ContentId blogPostItExternalId = new ExternalContentId("example.BlogPost");
    
    VersionedContentId inEditContentId = new VersionedContentId(blogPostContentId,
                                                                VersionedContentId.LATEST_VERSION);
    
    VersionedContentId previouslyCommittedContentId =
        new VersionedContentId(blogPostContentId, VersionedContentId.UNDEFINED_VERSION);
    
    VersionedContentId latestCommittedContentId =
        new VersionedContentId(blogPostContentId, VersionedContentId.LATEST_COMMITTED_VERSION);
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new EnsureBlogPostPresentCommand();
    }
    
    public void testNewBlogPostImage()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(fileItemMap.get("blogPostId")).thenReturn(null);
        
        when(cmServer.createContent(19, blogContentId, blogPostItExternalId)).thenReturn(blogPostPolicy);
        
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(latestCommittedContentId);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
    
        assertTrue("Returned false when all was successful.", doContinue);
        
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogPostId");
        verify(cmServer).createContent(19, blogContentId, blogPostItExternalId);
        verify(blogImageContext).setBlogPost(blogPostPolicy);
        verify(fckEditorUploadResponse).setBlogPostContentId(latestCommittedContentId);
    }
    
    public void testInEditBlogPostImage()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(fileItemMap.get("blogPostId")).thenReturn(blogPostIdFileItem);
        when(blogPostIdFileItem.getString("UTF-8")).thenReturn(inEditContentId.getContentIdString());
            
        when(cmServer.getPolicy(blogPostContentId)).thenReturn(null);
        when(cmServer.getPolicy(inEditContentId)).thenReturn(blogPostPolicy);
                
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(inEditContentId);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
    
        assertTrue("Returned false when all was successful.", doContinue);
    
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogPostId");
        verify(cmServer).getPolicy(inEditContentId);
        verify(cmServer, never()).createContent(19, blogPostItExternalId);
        verify(cmServer, never()).createContentVersion(inEditContentId);
        verify(blogImageContext).setBlogPost(blogPostPolicy);
        verify(fckEditorUploadResponse).setBlogPostContentId(inEditContentId);
    }
    
    public void testCommitedBlogPostImage()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(fileItemMap.get("blogPostId")).thenReturn(blogPostIdFileItem);
        when(blogPostIdFileItem.getString("UTF-8")).thenReturn(previouslyCommittedContentId.getContentIdString());
 
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(previouslyCommittedContentId);
        
        when(cmServer.createContentVersion(latestCommittedContentId)).thenReturn(blogPostPolicy);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
    
        assertTrue("Returned false when all was successful.", doContinue);
    
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogPostId");
        verify(cmServer, never()).createContent(19, blogPostItExternalId);
        verify(cmServer).createContentVersion(latestCommittedContentId);
        verify(cmServer, never()).getPolicy(blogPostContentId);
        verify(cmServer, never()).getPolicy(previouslyCommittedContentId);
        verify(blogImageContext).setBlogPost(blogPostPolicy);
        verify(fckEditorUploadResponse).setBlogPostContentId(previouslyCommittedContentId);
    }
    
    public void testCMException()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(fileItemMap.get("blogPostId")).thenReturn(null);
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        when(cmServer.createContent(19, blogContentId, blogPostItExternalId)).thenThrow(new CMException(""));

        boolean doContinue = toTest.execute(blogImageContext);
    
        assertFalse("Returned true when there was an error.", doContinue);
    
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogPostId");
        verify(blogPostIdFileItem, never()).getString("UTF-8");
        verify(cmServer).createContent(19, blogContentId, blogPostItExternalId);
        verify(cmServer, never()).getPolicy(blogPostContentId);
        verify(blogImageContext, never()).setBlogPost(blogPostPolicy);
    }
    
    public void testInvalidBlogPostId()
        throws Exception
    {
        when(blogImageContext.getRequest()).thenReturn(request);
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(fileItemMap.get("blogPostId")).thenReturn(blogPostIdFileItem);
        when(blogPostIdFileItem.getString("UTF-8")).thenReturn("illegalContentId");  
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
    
        assertFalse("Returned true when there was an error.", doContinue);
    
        verify(blogImageContext).getFileItemMap();
        verify(fileItemMap).get("blogPostId");
        verify(cmServer, never()).createContent(19, blogPostItExternalId);
        verify(cmServer, never()).getPolicy(blogPostContentId);
        verify(cmServer, never()).getPolicy(new VersionedContentId(19, 100, VersionedContentId.LATEST_VERSION));
        verify(blogImageContext, never()).setBlogPost(blogPostPolicy);
    }

    public void testNoPostYetCreatesContent()
        throws Exception
    {
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorUploadResponse);
        when(blogImageContext.getFileItemMap()).thenReturn(fileItemMap);
        when(fileItemMap.get("blogPostId")).thenReturn(blogPostIdFileItem);
        when(blogPostIdFileItem.getString("UTF-8")).thenReturn("");
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogImageContext.getCommunityMajor()).thenReturn(19);
        when(cmServer.createContent(19, blogContentId, blogPostItExternalId)).thenReturn(blogPostPolicy);
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);        
        
        boolean doContinue = toTest.execute(blogImageContext);
        assertTrue("Returned false when ok.", doContinue);        
        
        verify(cmServer).createContent(19, blogContentId, blogPostItExternalId);
    }
    
}
