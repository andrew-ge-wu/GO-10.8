package example.blog.command;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentInfo;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.list.ContentIdListSlice;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.blog.BlogContext;
import example.util.Context;
import example.util.RequestParameterUtil;

public class PrepareBlogViewCommand
    extends BlogPostCommandBase
{
    private static final Logger LOG = Logger.getLogger(PrepareBlogViewCommand.class.getName());
    private final RequestParameterUtil requestParameterUtil = new RequestParameterUtil();

    public boolean execute(Context context) {
        BlogContext blogContext = (BlogContext) context;
        
        CmClient cmClient = blogContext.getCmClient();
        RenderRequest renderRequest = blogContext.getRenderRequest();
        
        ModelWrite localModel = blogContext.getLocalModel();
        ContentId blogContentId = blogContext.getBlogContentId();
        
        int index = getIndex(renderRequest);
        int limit = 10;
    
        BlogPostList blogPostList =
            blogContext.getBlogPostListFactory().create(cmClient, blogContentId);
                
        try {
            ContentIdListSlice slice = null;
            if (blogContext.hasDateSelected()) {
                int year =  blogContext.getSelectedYear();
                int month = blogContext.getSelectedMonth();
                slice = blogPostList.getSliceByMonth(year, month, limit);
                index = slice.getStartIndex();
            } else {
                slice = blogPostList.getSlice(index, limit);
                setSelectedDateFromFirstContent(cmClient, localModel, slice);
            }
            localModel.setAttribute("index", index);
            localModel.setAttribute("posts", slice.getContentIds());
            localModel.setAttribute("postsLimit", limit);
            if (slice.getNextSliceStartIndex() > 0) {
                localModel.setAttribute("nextIndex", slice.getNextSliceStartIndex());
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to get blog post list", e);
        }
        
        return true;
    }

    private void setSelectedDateFromFirstContent(CmClient cmClient, ModelWrite localModel,
                                                 ContentIdListSlice slice)
        throws CMException
    {
        if (slice.getContentIds().size() > 0) {
            ContentId cid = slice.getContentIds().get(0);
            ContentInfo ci  = cmClient.getCMServer().getContentInfo(cid);
            long created = ci.getCreationTime();
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC Universal"));
            calendar.setTimeInMillis(created);
            localModel.setAttribute("selectedYear", calendar.get(Calendar.YEAR));
            localModel.setAttribute("selectedMonth", calendar.get(Calendar.MONTH));
        }
    }
    
    private int getIndex(RenderRequest request)
    {
        return requestParameterUtil.getInt(request, "index", 0, 0, Integer.MAX_VALUE);
    }

}
