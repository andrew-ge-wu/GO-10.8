package example.blog.image.command;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.MockitoBase;
import example.blog.BlogPolicy;
import example.blog.image.BlogImageContext;
import example.blog.image.FckEditorUploadResponse;
import example.membership.UserHandler;

public class AssertUserAllowedCommandTest
    extends MockitoBase
{
    private AssertUserAllowedCommand toTest = null;
    
    @Mock BlogImageContext blogImageContext;

    @Mock PolicyCMServer cmServer;
    @Mock HttpServletRequest request;
    @Mock UserHandler userHandler;
    
    @Mock BlogPolicy blogPolicy;
    
    @Mock User user;
    @Mock UserId userId;
    
    ContentId blogContentId = new ContentId(19, 100);
    
    @Mock FckEditorUploadResponse fckEditorResponse;
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        toTest = new AssertUserAllowedCommand();
    }
    
    public void testUserAllowed()
        throws Exception
    {
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogImageContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogImageContext.getRequest()).thenReturn(request);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                (HttpServletResponse) eq(null))).thenReturn(user);
        
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        when(user.getUserId()).thenReturn(userId);
        
        when(blogPolicy.isAllowedToEdit(userId)).thenReturn(true);
        
        when(user.getUserId()).thenReturn(userId);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
                
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertTrue("User was not allowed to upload blog image, even though logged in " +
                        "and in owner set.", doContinue);
        
        verify(fckEditorResponse, never()).setStatus(FckEditorUploadResponse.Status.OK);
    }
    
    public void testUserNotLoggedIn()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(userId);
    
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogImageContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogImageContext.getRequest()).thenReturn(request);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                                         (HttpServletResponse) eq(null))).thenReturn(null);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("User was allowed to upload blog image, even though not logged in.", doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.PERMISSION_DENIED);
    }
    
    public void testUserNotOwner()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(new UserId("anotherUser"));
    
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogImageContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogImageContext.getRequest()).thenReturn(request);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                                         (HttpServletResponse) eq(null))).thenReturn(user);
        
        when(cmServer.getPolicy(blogContentId)).thenReturn(blogPolicy);
        when(blogPolicy.getOwnerIds()).thenReturn(blogOwnerIds);
        
        when(user.getUserId()).thenReturn(userId);
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("User was allowed to edit, even though not owner.", doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.PERMISSION_DENIED);
    }
    
    public void testCMException()
        throws Exception
    {
        Set<UserId> blogOwnerIds = new HashSet<UserId>();
        blogOwnerIds.add(new UserId("anotherUser"));
    
        when(blogImageContext.getPolicyCMServer()).thenReturn(cmServer);
        when(blogImageContext.getBlogContentId()).thenReturn(blogContentId);
        when(blogImageContext.getUserHandler()).thenReturn(userHandler);
        
        when(blogImageContext.getRequest()).thenReturn(request);
        
        when(userHandler.getLoggedInUser(any(HttpServletRequest.class),
                                         (HttpServletResponse) eq(null))).thenReturn(user);
        
        when(cmServer.getPolicy(blogContentId)).thenThrow(new CMException(""));
        
        when(blogImageContext.getFckEditorResponse()).thenReturn(fckEditorResponse);
        
        boolean doContinue = toTest.execute(blogImageContext);
        
        assertFalse("User was allowed to edit, even though cmexception.", doContinue);
        
        verify(fckEditorResponse).setStatus(FckEditorUploadResponse.Status.SERVER_ERROR);
    }
}
