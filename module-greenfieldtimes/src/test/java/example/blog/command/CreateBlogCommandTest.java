package example.blog.command;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;

import example.MockitoBase;
import example.blog.BlogContext;
import example.blog.BlogFormValidator;
import example.blog.BlogPolicy;
import example.blog.HTMLValidator;
import example.membership.RenderControllerProfileElement;

public class CreateBlogCommandTest extends MockitoBase {

    private BlogCommandBase target;
    @Mock private RenderHttpServletRequest request;
    @Mock private BlogContext blogContext;
    @Mock CmClient cmClient;
    @Mock PolicyCMServer cmServer;
    @Mock ModelWrite localModel;
    @Mock TopModel topModel;
    @Mock BlogFormValidator formValidator;
    @Mock User loggedInUser;
    @Mock UserId userId;
    @Mock BlogPolicy blog;
    @Mock HTMLValidator htmlValidator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        target = new CreateBlogCommand(htmlValidator);
    }

    public void testShouldTranslateSiteParentToContentId() throws Exception {
        ContentId contentId = new ContentId(1, 200);
        when(request.getParameter(RenderControllerProfileElement.REQUEST_PARAMETER_PARENT_PAGE))
            .thenReturn("1.200");
        assertEquals(contentId, target.getParentPage(request));
    }

    public void testShouldNotCreateBlogIfFormInvalid()
        throws Exception
    {
        when(blogContext.getRenderRequest()).thenReturn(request);
        when(blogContext.getTopModel()).thenReturn(topModel);
        when(topModel.getLocal()).thenReturn(localModel);

        assertTrue("Execute should return true", target.execute(blogContext));

        verifyNoMoreInteractions(cmServer);
    }

    public void testShouldCleanInput() throws Exception {

        String dirtyText = "dirtyText";
        String cleanedText = "cleanedText";
        when(htmlValidator.stripAllHTML(anyString())).thenReturn(cleanedText);
        when(htmlValidator.getCleanHTML(anyString())).thenReturn(cleanedText);

        when(request.getParameter("blog_name")).thenReturn(dirtyText);
        when(request.getParameter("blog_description")).thenReturn(dirtyText);

        assertEquals(cleanedText, target.getBlogName(request));
        assertEquals(cleanedText, target.getBlogDescription(request));
    }

    interface RenderHttpServletRequest extends RenderRequest, HttpServletRequest {}
}
