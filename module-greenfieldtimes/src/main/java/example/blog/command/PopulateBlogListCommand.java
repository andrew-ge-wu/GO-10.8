package example.blog.command;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.model.Model;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.membership.UserDataManager;
import com.polopoly.siteengine.membership.UserDataManager.ServiceInfo;
import com.polopoly.user.server.User;

import example.blog.BlogContext;
import example.blog.UserBlogsData;
import example.membership.RenderControllerProfileElement;
import example.membership.UserHandler;
import example.util.Command;
import example.util.Context;

public class PopulateBlogListCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(PopulateBlogListCommand.class.getName());

    public boolean execute(Context context)
    {
        if (context instanceof BlogContext) {
            BlogContext blogContext = (BlogContext) context;

            RenderRequest renderRequest = blogContext.getRenderRequest();
            UserHandler userHandler = blogContext.getUserHandler();
            UserDataManager userDataManager = blogContext.getUserDataManager();

            try {
                User loggedInUser = userHandler.getLoggedInUser(((HttpServletRequest) renderRequest), null);
                if (loggedInUser == null) {
                    return false;
                }
                ServiceInfo[] serviceInfos = userDataManager.getServiceInfos(loggedInUser.getUserId());
                
                for (ServiceInfo info : serviceInfos) {
                    if (info.getServiceId().equals(RenderControllerProfileElement.SERVICE_ID_BLOG)) {
                        UserBlogsData blogServiceBean = (UserBlogsData)userDataManager.getOrCreateServiceBean(blogContext.getLoggedInUser().getUserId(), info.getServiceId());
                        
                        ModelWrite localModel = blogContext.getTopModel().getLocal();
                        
                        List<Model> blogs = getBlogs(blogServiceBean, blogContext);
                        localModel.setAttribute("blogs", blogs);
                        
                        return true;
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Unable to set blog descriptions in model", e);
                blogContext.addBlogErrorMessageToLocalModel("error", "readError");
            }
        }
        
        return false;
    }
    
    private List<Model> getBlogs(UserBlogsData blogServiceBean,
                                 BlogContext context)
        throws CMException
    {
        PolicyModelDomain policyModelDomain =
            (PolicyModelDomain) context.getControllerContext().getModelDomain();
        
        List<Model> blogs = new ArrayList<Model>();
        for (String blogContentIdString : blogServiceBean.getBlogs()) {
            ContentId blogContentId = ContentIdFactory.createContentId(blogContentIdString);
            
            Model model = policyModelDomain.getModel(blogContentId);
            blogs.add(model);
        }

        return blogs;
    }
    
}
