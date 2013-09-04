package example.blog.command;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.community.blog.BlogPostList;
import com.polopoly.community.blog.BlogYear;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;

import example.blog.BlogContext;
import example.util.Context;

public class PrepareBlogPageViewCommand extends BlogPostCommandBase {

    private static final Logger LOG = Logger.getLogger(PrepareBlogViewCommand.class.getName());
    
    public boolean execute(Context context) {
        
        BlogContext blogContext = (BlogContext) context;
        
        CmClient cmClient = blogContext.getCmClient();
        RenderRequest renderRequest = blogContext.getRenderRequest();
        
        ModelWrite localModel = blogContext.getLocalModel();
        ContentId blogContentId = blogContext.getBlogContentId();
        
        int month = getMonth(renderRequest);
        int year  = getYear(renderRequest);

        BlogPostList blogPostList =
            blogContext.getBlogPostListFactory().create(cmClient,
                                                        blogContentId);
        try {
            List<BlogYear> blogYears = blogPostList.getBlogYears();
            localModel.setAttribute("blogYears", blogYears);

            if (year > -1) {
                if (month < 0) {
                    for (BlogYear blogYear: blogYears) {
                        if (blogYear.getYear() == year) {
                            month = blogYear.getMonths().get(0).getMonth();
                        }
                    }
                }
                localModel.setAttribute("selectedYear", year);
                localModel.setAttribute("selectedMonth", month);
            }
            
            
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to get blog post list", e);
        }
        
        return true;
    }
    
    protected int getYear(RenderRequest request)
    {
        return getInt(request, "year", -1, 1900, 2500);
    }

    protected int getMonth(RenderRequest request)
    {
        return getInt(request, "month", -1, 0, 11);
    }
    
    protected int getInt(RenderRequest request,
                         String key,
                         int defaultValue,
                         int minValue,
                         int maxValue)
    {
        try {
            int value = Integer.parseInt(request.getParameter(key));

            if (value > maxValue) {
                value = maxValue;
            } else if (value < minValue) {
                value = minValue;
            }

            return value;
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

}
