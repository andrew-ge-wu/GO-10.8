package example.blog;

import org.mockito.Mock;
import org.mockito.Mockito;

import example.MockitoBase;


public class BlogFormValidatorTest extends MockitoBase {
    
    private BlogFormValidator target;
    @Mock private BlogContext blogContext;
    @Mock private BlogForm blogForm;
    
    @Override
    protected void setUp() throws Exception {     
        super.setUp();        
        target = new BlogFormValidator();        
    }
    
    public void testShouldReturnTrueIfValidBlogForm() throws Exception {
        
        Mockito.when(blogForm.getBlogName()).thenReturn("Blog name");
        Mockito.when(blogForm.getBlogAddress()).thenReturn("Blog-address");
        
        boolean validateResult = target.validate(blogContext, blogForm);
        assertTrue("Should return true if valid form", validateResult);
        
    }
    
    public void testShouldReturnFalseIfBlogNameIsInvalid() throws Exception {
        
        Mockito.when(blogForm.getBlogName()).thenReturn("");
        
        boolean validateResult = target.validate(blogContext, blogForm);
        
        Mockito.verify(blogContext).addErrorShowBlogEdit("fieldRequiredBlogName");
        assertFalse("Should return false if not valid form", validateResult);
    }
    
    public void testShouldReturnFalseIfNoBlogAddressIsSpecified() throws Exception {
        Mockito.when(blogForm.getBlogAddress()).thenReturn("");
        
        boolean validateResult = target.validate(blogContext, blogForm);
        
        Mockito.verify(blogContext).addErrorShowBlogEdit("fieldRequiredBlogAddress");
        
        assertFalse("Should have returned false when no blog address specified", 
                validateResult);
    }
    
    public void testShouldReturnFalseIfBlogAddressContainsInvalidCharacters() throws Exception {
        
        Mockito.when(blogForm.getBlogAddress()).thenReturn("Min k\u00e4rlek till Atex");
        
        boolean validateResult = target.validate(blogContext, blogForm);
        
        Mockito.verify(blogContext).addErrorShowBlogEdit("fieldInvalidBlogAddress");
        
        assertFalse("Should have returned false when the blog address contains invalid characters", validateResult);
    }
}
