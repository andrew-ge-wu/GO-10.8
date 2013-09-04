package example.blog.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.blog.BlogPostListFactory;
import com.polopoly.community.list.ContentIdListSlice;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.MockitoBase;
import example.blog.BlogContext;
import example.blog.BlogPolicy;
import example.membership.UserHandler;

public class PrepareBlogViewCommandTest
    extends MockitoBase
{
    PrepareBlogViewCommand toTest;
    
    @Mock BlogContext blogContext;
    @Mock CmClient cmClient;
    @Mock PolicyCMServer cmServer;
    @Mock UserHandler userHandler;
    @Mock RenderRequest renderRequest;
    @Mock ModelWrite localModel;
    
    @Mock BlogPolicy blogPolicy;
    @Mock BlogPostList blogPostList;
    @Mock ContentIdListSlice slice;
    ContentId blogContentId = new ContentId(19, 100);
    
    @Mock User correctlyLoggedInUser;
    @Mock UserId correctlyLoggedInUserId;
    
    @Mock BlogPostListFactory blogPostListFactory;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new PrepareBlogViewCommand();
    }
    
    public void testPrepareBlogPostsView()
        throws Exception
    {
        when(blogContext.getCmClient()).thenReturn(cmClient);
        
        when(blogContext.getBlogPostListFactory()).thenReturn(blogPostListFactory);
        when(blogPostListFactory.create(cmClient, blogContentId)).thenReturn(blogPostList);
        when(blogPostList.getSlice(0, 10)).thenReturn(slice);
        
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        when(renderRequest.getParameter("index")).thenReturn("0");
        
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        when(slice.getNextSliceStartIndex()).thenReturn(-1);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("Returned false, should always return true.", doContinue);
        verify(localModel).setAttribute(eq("posts"), any(List.class));
        verify(localModel, never()).setAttribute(eq("nextIndex"), any(Integer.class));
    }
    
    public void testPrepareBlogPostsViewHasNext()
        throws Exception
    {
        when(blogContext.getCmClient()).thenReturn(cmClient);
        
        when(blogContext.getBlogPostListFactory()).thenReturn(blogPostListFactory);
        when(blogPostListFactory.create(cmClient, blogContentId)).thenReturn(blogPostList);
        when(blogPostList.getSlice(0, 10)).thenReturn(slice);
        
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        when(renderRequest.getParameter("index")).thenReturn("0");
        
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        when(slice.getNextSliceStartIndex()).thenReturn(10);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("Returned false, should always return true.", doContinue);
        verify(localModel).setAttribute(eq("posts"), any(List.class));
        verify(localModel).setAttribute(eq("nextIndex"), eq(10));
    }
    
    public void testCMException()
        throws Exception
    {
        when(blogContext.getCmClient()).thenReturn(cmClient);
        
        when(blogContext.getBlogPostListFactory()).thenReturn(blogPostListFactory);
        when(blogPostListFactory.create(cmClient, blogContentId)).thenReturn(blogPostList);
        
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        when(renderRequest.getParameter("index")).thenReturn("0");
        
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);

        when(blogContext.getLocalModel()).thenReturn(localModel);
        when(blogPostList.getSlice(0, 10)).thenThrow(new CMException(""));

        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("Returned false, should always return true.", doContinue);
        
        verify(localModel, never()).setAttribute(eq("posts"), any(List.class));
        verify(localModel, never()).setAttribute(eq("nextIndex"), any(Integer.class));
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }
}
