package example.blog;

import com.polopoly.siteengine.util.FriendlyUrlConverter;
import com.polopoly.util.StringUtil;

public class BlogFormValidator {
    
    public boolean validate(BlogContext blogContext, BlogForm blogForm) {
        
        boolean isValid = true;
        
        String blogName = blogForm.getBlogName();
        if (StringUtil.isEmpty(blogName)) {
            blogContext.addErrorShowBlogEdit(RenderControllerBlog.FIELD_REQUIRED_BLOG_NAME);
            isValid = false;
        }
        
        String blogAddress = blogForm.getBlogAddress();
        if (StringUtil.isEmpty(blogAddress)) {
            blogContext.addErrorShowBlogEdit(RenderControllerBlog.FIELD_REQUIRED_BLOG_ADDRESS);
            isValid = false;
        }
        else if (!(FriendlyUrlConverter.convert(blogAddress).equals(blogAddress))) {
            blogContext.addErrorShowBlogEdit(RenderControllerBlog.FIELD_INVALID_BLOG_ADDRESS);
            isValid = false;
        }
        
        return isValid;
    }
}
