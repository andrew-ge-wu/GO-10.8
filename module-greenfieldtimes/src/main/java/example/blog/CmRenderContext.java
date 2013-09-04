package example.blog;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.util.StringUtil;

import example.membership.UserHandler;
import example.util.Context;
import example.util.ContextImplBase;

public class CmRenderContext extends ContextImplBase implements Context
{
    private final RenderRequest _request;
    private final TopModel _topModel;
    private final CmClient _cmClient;
    private final ControllerContext _controllerContext;
    private final UserHandler _userHandler;

    public CmRenderContext(RenderRequest request, TopModel topModel,
                           ControllerContext controllerContext, CmClient cmClient,
                           UserHandler userHandler)
    {
        _request = request;
        _topModel = topModel;
        _controllerContext = controllerContext;
        _cmClient = cmClient;
        _userHandler = userHandler;
    }

    public RenderRequest getRenderRequest()
    {
        return _request;
    }

    public CmClient getCmClient()
    {
        return _cmClient;
    }

    public ModelWrite getLocalModel()
    {
        return _topModel.getLocal();
    }

    public PolicyCMServer getPolicyCMServer()
    {
        return _cmClient.getPolicyCMServer();
    }
    
    public TopModel getTopModel()
    {
        return _topModel;
    }

    public ContentId getCurrentContentId()
    {
        return _controllerContext.getContentId();
    }
    
    public ControllerContext getControllerContext()
    {
        return _controllerContext;
    }
    
    public UserHandler getUserHandler()
    {
        return _userHandler;
    }
    
    public ContentId getContentIdFromRequest(String parameterName)
    {
        String idString = getRenderRequest().getParameter(parameterName);
        if (!StringUtil.isEmpty(idString)) {
            try {
                return ContentIdFactory.createContentId(idString);
            }
            catch (IllegalArgumentException e) {}
        }
        return null;
    }
    
}
