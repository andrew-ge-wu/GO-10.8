package example.blog.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import example.blog.BlogPolicy;
import example.membership.UserHandler;

public class AssertUserAllowedCommandTest
    extends MockitoBase
{
    AssertUserAllowedCommand toTest;
 
    @Mock BlogContext blogContext;
    @Mock PolicyCMServer cmServer;
    @Mock UserHandler userHandler;
    @Mock(extraInterfaces={HttpServletRequest.class}) RenderRequest renderRequest;
    @Mock ModelWrite localModel;
    
    @Mock BlogPolicy blogPolicy;
    ContentId blogContentId = new ContentId(19, 100);
    
    @Mock User correctlyLoggedInUser;
    @Mock UserId correctlyLoggedInUserId;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new AssertUserAllowedCommand();
    }
    
    public void testUserAllowed()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(correctlyLoggedInUserId);

        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                (HttpServletResponse) eq(null))).thenReturn(correctlyLoggedInUser);
        
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        when(blogPolicy.isAllowedToEdit(correctlyLoggedInUserId)).thenReturn(true);
        
        when(correctlyLoggedInUser.getUserId()).thenReturn(correctlyLoggedInUserId);
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertTrue("User was not allowed to edit, even though logged in " +
                   "and in owner set.", doContinue);
        
        verify(localModel, never()).setAttribute(eq("error"), any(List.class));
    }
    
    public void testUserNotLoggedIn()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(correctlyLoggedInUserId);
    
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                (HttpServletResponse) eq(null))).thenReturn(null);
        
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertFalse("User was allowed to edit, even though not logged in.", doContinue);
        verify(blogContext).addErrorShowBlogView("permissionDenied");
    }
    
    public void testUserNotOwner()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(new UserId("anotherUser"));
    
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                (HttpServletResponse) eq(null))).thenReturn(correctlyLoggedInUser);
        
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        when(blogPolicy.isAllowedToEdit(correctlyLoggedInUserId)).thenReturn(false);
        
        when(correctlyLoggedInUser.getUserId()).thenReturn(correctlyLoggedInUserId);
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertFalse("User was allowed to edit, even though not owner.", doContinue);
        verify(blogContext).addErrorShowBlogView("permissionDenied");
    }
    
    public void testCMException()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(new UserId("anotherUser"));
    
        when(blogContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogContext.getRenderRequest()).thenReturn(renderRequest);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                (HttpServletResponse) eq(null))).thenReturn(correctlyLoggedInUser);
        
        when(cmServer.getPolicy(blogContentId)).thenThrow(new CMException(""));
        when(blogContext.getLocalModel()).thenReturn(localModel);
        
        boolean doContinue = toTest.execute(blogContext);
        
        assertFalse("User was allowed to edit, even though cmexception.", doContinue);
        verify(blogContext).addErrorShowBlogView("internalServerError");
    }
}
