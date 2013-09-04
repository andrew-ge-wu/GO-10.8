package example.content.article;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.mockito.Mock;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Metadata;

import example.MockitoBase;
import example.test.MetadataAwarePolicy;

public class StandardArticlePolicyTest extends MockitoBase {

  
    private StandardArticlePolicy target;

    @Mock private PolicyCMServer cmServer;
    @Mock private Content content;
    @Mock private InputTemplate inputTemplate;
    @Mock private Policy parent;
    @Mock private HashMap<String,Policy> children;
    @Mock private SingleValuePolicy body;
    @Mock private SingleValuePolicy lead;
    @Mock private SingleValuePolicy title;

    @Mock private MetadataAwarePolicy categorizationProvider;

    private Metadata categorization = new Metadata();
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        target = new StandardArticlePolicy() {
            @Override
            protected synchronized void initChildPolicies() throws CMException
            {
                this.childPolicies = children;
            }
        };
        target.init("policy", new Content[] { content }, inputTemplate, parent,
                    cmServer);
                
        when(children.get("body")).thenReturn(body);
        when(children.get("lead")).thenReturn(lead);
        when(children.get("name")).thenReturn(title);
    }

    
    public void testShouldConverNullAnswersToEmptyString() throws Exception
    {
        when(title.getValue()).thenReturn(null);
        when(body.getValue()).thenReturn(null);
        when(lead.getValue()).thenReturn(null);
        
        String text = target.getTextRepresentation();
        assertEquals("", text);
    }
    
    
    public void testShouldDelegateGetCategorizationToCategorizationChildPolicy() throws Exception 
    {
        when(children.get("categorization")).thenReturn(categorizationProvider);
        when(categorizationProvider.getMetadata()).thenReturn(categorization);
        
        Metadata result = target.getMetadata();
        assertEquals(categorization, result);
    }
    
    public void testShouldDelegateSetCategorizationToCategorizationChildPolicy() throws Exception 
    {
        when(children.get("categorization")).thenReturn(categorizationProvider);
        
        target.setMetadata(categorization);
        verify(categorizationProvider).setMetadata(categorization);
        
    }
}
