package example.blog.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.MockitoBase;
import example.blog.AntiSamyHTMLValidator;
import example.blog.BlogContext;
import example.blog.BlogPostPolicy;
import example.blog.InvalidHTMLException;
import example.membership.UserHandler;

public class BlogPostCommandTest
    extends MockitoBase
{
    private static final ExternalContentId INPUT_TEMPLATE_ID = new ExternalContentId("example.BlogPost");

    BlogPostCommand toTest;
 
    @Mock BlogContext blogContext;
    @Mock PolicyCMServer cmServer;
    @Mock UserHandler userHandler;
    @Mock RenderRequest renderRequest;
    @Mock ModelWrite localModel;
    @Mock ContentRead blogPost;
    @Mock CmClient cmClient;
    
    @Mock BlogPostPolicy blogPostPolicy;
    @Mock BlogPostListFactory blogPostListFactory;
    @Mock AntiSamyHTMLValidator validator;
    
    @Mock BlogPostList blogPostList;
    @Mock Content blogPostContent;
    
    VersionedContentId blogContentId = new VersionedContentId(19, 100, 0);
    VersionedContentId commitedBlogPostContentId = new VersionedContentId(19, 200, VersionedContentId.UNDEFINED_VERSION);
    VersionedContentId uncommitedBlogPostContentId = new VersionedContentId(19, 200, 17);
    
    @Mock User correctlyLoggedInUser;
    @Mock UserId correctlyLoggedInUserId;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new BlogPostCommand(validator);
    }
    
    public void testCreateBlogPostWithoutImageUpload()
        throws Exception
    {
        mockRequestData(null);
        when(blogContext.getAction()).thenReturn(PostActionSplitter.Action.CREATE_BLOG_POST);
        when(cmServer.createContent(19, blogContentId, INPUT_TEMPLATE_ID)).thenReturn(blogPostPolicy);
        mockAddToList();
        
        boolean doContinue = toTest.execute(blogContext);
        assertTrue("Should return true when succeding.", doContinue);
        
        verify(cmServer).createContent(19, blogContentId, INPUT_TEMPLATE_ID);
        verify(cmServer).commitContent(blogPostPolicy);
        verify(blogPostList).addFirst(commitedBlogPostContentId.getContentId());
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }

    public void testCreateBlogPostWithImageUpload() throws Exception {
        mockRequestData(uncommitedBlogPostContentId);
        when(blogContext.getAction()).thenReturn(PostActionSplitter.Action.CREATE_BLOG_POST);
        when(cmServer.getPolicy(any(ContentId.class))).thenReturn(blogPostPolicy);
        mockAddToList();
        
        boolean doContinue = toTest.execute(blogContext);
        assertTrue("Should return true when succeding.", doContinue);
        
        verify(cmServer).getPolicy(uncommitedBlogPostContentId);
        verify(cmServer).commitContent(blogPostPolicy);
        verify(blogPostList).addFirst(commitedBlogPostContentId.getContentId());
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }

    public void testEditBlogPostWithoutImageUpload() throws Exception {
        mockRequestData(commitedBlogPostContentId);
        when(blogContext.getAction()).thenReturn(PostActionSplitter.Action.EDIT_BLOG_POST);
        when(cmServer.createContentVersion(any(VersionedContentId.class))).thenReturn(blogPostPolicy);
        mockAddToList();
        
        boolean doContinue = toTest.execute(blogContext);
        assertTrue("Should return true when succeding.", doContinue);
        
        verify(cmServer).createContentVersion(commitedBlogPostContentId.getLatestCommittedVersionId());
        verify(cmServer).commitContent(blogPostPolicy);
        verify(blogPostList, never()).addFirst(commitedBlogPostContentId.getContentId());
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }
    
    public void testEditBlogPostWithImageUpload() throws Exception {
        mockRequestData(uncommitedBlogPostContentId);
        when(blogContext.getAction()).thenReturn(PostActionSplitter.Action.EDIT_BLOG_POST);
        when(cmServer.getPolicy(any(VersionedContentId.class))).thenReturn(blogPostPolicy);
        mockAddToList();
        
        boolean doContinue = toTest.execute(blogContext);
        assertTrue("Should return true when succeding.", doContinue);
        
        verify(cmServer).getPolicy(uncommitedBlogPostContentId);
        verify(cmServer).commitContent(blogPostPolicy);
        verify(blogPostList, never()).addFirst(commitedBlogPostContentId.getContentId());
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }
    
    public void testCreateWithCMEXception()
        throws Exception
    {
        mockRequestData(null);
        when(blogContext.getAction()).thenReturn(PostActionSplitter.Action.CREATE_BLOG_POST);
        when(cmServer.createContent(19, blogContentId, INPUT_TEMPLATE_ID)).thenThrow(new CMException(""));
        
        boolean doContinue = toTest.execute(blogContext);
        assertFalse("Should return false on error.", doContinue);
        
        verify(cmServer).createContent(19, blogContentId, INPUT_TEMPLATE_ID);
        verify(cmServer, never()).commitContent(blogPostPolicy);
        verify(blogPostList, never()).addFirst(commitedBlogPostContentId.getContentId());
        verify(blogContext).addErrorShowBlogPostCreate("createBlogPostError");
    }

    private void mockRequestData(VersionedContentId blogPostId) throws CMException, InvalidHTMLException
    {
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        when(blogContext.getLocalModel()).thenReturn(localModel);
        when(blogContext.getCmClient()).thenReturn(cmClient);
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getBlogPostContentId()).thenReturn(blogPostId);
        when(cmServer.getMajorByName(DefaultMajorNames.COMMUNITY)).thenReturn(19);
        when(renderRequest.getParameter("blog_post_heading")).thenReturn("blog post name");
        when(renderRequest.getParameter("blog_post_text")).thenReturn("blog post text");
        when(validator.stripAllHTML("blog post name")).thenReturn("blog post name");
        when(validator.getCleanHTML("blog post text")).thenReturn("blog post text");
    }
    
    private void mockAddToList()
    {
        when(blogContext.getBlogPostListFactory()).thenReturn(blogPostListFactory);
        when(blogPostListFactory.create(cmClient, blogContentId)).thenReturn(blogPostList);
        when(blogPostPolicy.getContent()).thenReturn(blogPostContent);
        when(blogPostContent.getContentId()).thenReturn(commitedBlogPostContentId);
    }

}
