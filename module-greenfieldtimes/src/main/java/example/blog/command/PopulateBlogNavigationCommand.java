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
import example.util.Command;
import example.util.Context;
import example.util.RequestParameterUtil;

public class PopulateBlogNavigationCommand implements Command
{
    private static final Logger LOG = Logger.getLogger(PopulateBlogNavigationCommand.class.getName());
    private final RequestParameterUtil requestParameterUtil = new RequestParameterUtil();
    
    public boolean execute(Context context) {
        BlogContext blogContext = (BlogContext) context;
        
        CmClient cmClient = blogContext.getCmClient();
        RenderRequest renderRequest = blogContext.getRenderRequest();
        
        ModelWrite localModel = blogContext.getLocalModel();
        ContentId blogContentId = blogContext.getBlogContentId();
        
        int year  = getYear(renderRequest);
        int month = getMonth(renderRequest);
            
        BlogPostList blogPostList =
            blogContext.getBlogPostListFactory().create(cmClient,
                                                        blogContentId);
                
        try {
            List<BlogYear> blogYears = blogPostList.getBlogYears();
        
            if (year > -1) {
                if (month < 0) {
                    for (BlogYear blogYear: blogYears) {
                        if (blogYear.getYear() == year) {
                            month = blogYear.getMonths().get(0).getMonth();
                        }
                    }
                }
                localModel.setAttribute("selectedYear", year);
                blogContext.setSelectedYear(year);
                
                localModel.setAttribute("selectedMonth", month);
                blogContext.setSelectedMonth(month);
                
            } else {
            }
            localModel.setAttribute("blogYears", blogYears);
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to get blog post list", e);
        }
        
        return true;
    }
    
    private int getYear(RenderRequest request)
    {
        return requestParameterUtil.getInt(request, "year", -1, 1900, 2500);
    }

    private int getMonth(RenderRequest request)
    {
        return requestParameterUtil.getInt(request, "month", -1, 0, 11);
    }

}
