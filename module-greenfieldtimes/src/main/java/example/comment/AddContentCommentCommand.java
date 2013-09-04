package example.comment;

import java.util.ArrayList;
import java.util.List;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.community.comment.CommentList;
import com.polopoly.model.ModelWrite;

import example.blog.AntiSamyHTMLValidator;
import example.blog.CmRenderContext;
import example.blog.command.AddCommentCommand;
import example.layout.element.comments.CommentsElementPolicy;
import example.layout.element.comments.RenderControllerCommentsElement;
import example.util.Context;

public class AddContentCommentCommand extends AddCommentCommand
{
    public AddContentCommentCommand(AntiSamyHTMLValidator validator)
    {
        super(validator);
    }
    
    @SuppressWarnings("unchecked")
    public void showError(Context context, String errorKey)
    {
        CmRenderContext cmContext = (CmRenderContext) context;
        ModelWrite localModel = cmContext.getLocalModel();
        List<String> errorList = (List<String>) localModel.getAttribute("error");
        if (errorList == null) {
            errorList = new ArrayList<String>();
            localModel.setAttribute("error", errorList);
        }
        errorList.add(errorKey);
        localModel.setAttribute(RenderControllerCommentsElement.HAS_COMMENT_ERROR,
                                Boolean.TRUE);
    }

    public CommentList getCommentList(Context context)
        throws CMException
    {
        CmRenderContext cmContext = (CmRenderContext) context;
        ContentId elementId = cmContext.getCurrentContentId();
        CommentsElementPolicy policy = (CommentsElementPolicy)
            cmContext.getPolicyCMServer().getPolicy(elementId);
        return policy.getCommentList();
    }

    protected ContentId getCommentParent(Context context)
    {
        return ((CmRenderContext) context).getCurrentContentId().getContentId();
    }

    protected boolean isPostingToThisContent(CmRenderContext cmContext)
    {
        return cmContext.getCurrentContentId().equalsIgnoreVersion(getCommentsIdPostParameter(cmContext));
    }
}
