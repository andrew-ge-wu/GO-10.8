package example.blog;

import static org.mockito.Mockito.verify;

import org.mockito.Mock;

import com.polopoly.cm.client.filter.state.DeleteState;

import example.MockitoBase;

public class BlogPostPolicyTest extends MockitoBase{

   BlogPostPolicy target;
   @Mock DeleteState deleteState;
 
   @Override
   protected void setUp() throws Exception {
       super.setUp();
       target = new BlogPostPolicy();
       target.deleteState = deleteState;
   }


   public void testShouldMarkBlogPostAsDeleted() throws Exception {
       target.delete();       
       verify(deleteState).delete(target);
   }
}
