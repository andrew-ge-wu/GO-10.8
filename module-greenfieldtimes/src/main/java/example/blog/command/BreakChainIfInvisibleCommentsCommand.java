package example.blog.command;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.client.CMException;
import com.polopoly.community.comment.CommentList;
import com.polopoly.community.comment.CommentListFactory;

import example.blog.CmRenderContext;
import example.layout.element.comments.RenderControllerCommentsElement;
import example.util.Command;
import example.util.Context;
import example.util.RequestParameterUtil;

public class BreakChainIfInvisibleCommentsCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(BreakChainIfInvisibleCommentsCommand.class.getName());
    
    public boolean execute(Context context)
    {
        CmRenderContext cmRenderContext = (CmRenderContext) context;

        boolean putCommentsIntoModel;
        
        if (new RequestParameterUtil().isAjaxRequestMode(cmRenderContext.getRenderRequest())) {
            putCommentsIntoModel = true;
        } else {
            putCommentsIntoModel = isMobileMode(cmRenderContext);
        }
        
        if (!putCommentsIntoModel) {
            CommentList list = new CommentListFactory().create(cmRenderContext.getCmClient(),
                                                               cmRenderContext.getCurrentContentId());
            try {
                int cntItems = list.getSlice(0, 1).getContentIds().size();
                if (cntItems != 0) {
                    cmRenderContext
                        .getLocalModel()
                        .setAttribute(RenderControllerCommentsElement.HAS_COMMENTS, Boolean.TRUE);
                }
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Unable to get comments", e);
            }
        }
        
        return putCommentsIntoModel;
    }
    
    protected boolean isMobileMode(CmRenderContext context)
    {
        return "mobile".equals(context.getControllerContext().getMode());
    }
}
