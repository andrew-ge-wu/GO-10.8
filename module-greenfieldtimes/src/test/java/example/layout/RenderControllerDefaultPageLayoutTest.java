package example.layout;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.policy.Policy;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.model.ModelWrite;
import com.polopoly.siteengine.model.TopModel;

import example.MockitoBase;
import example.test.MetadataAwarePolicy;

public class RenderControllerDefaultPageLayoutTest extends MockitoBase {

    private RenderControllerDefaultPageLayout target;
    @Mock private Policy lastContent;
    @Mock private TopModel m;
    @Mock private ModelWrite localModel;
    @Mock private MetadataAwarePolicy categorizationProviderPolicy;
    private Metadata categorization;

    protected void setUp() throws Exception
    {
        super.setUp();
        target = new RenderControllerDefaultPageLayout();
        when(m.getLocal()).thenReturn(localModel);
        Dimension subject = new Dimension("department.categorydimension.subject", "Subject", true,
                new Entity("id1", "chemistry"),
                new Entity("id2", "politics"));
        Dimension person = new Dimension("department.categorydimension.tag.Person", "Person", false,
                new Entity("id3", "emma grey"),
                new Entity("id4", "tom tom"));
        Dimension company = new Dimension("department.categorydimension.tag.Company", "Company", false,
                new Entity("id5", "atex"));
        Dimension location = new Dimension("department.categorydimension.tag.Location", "Location", false,
                new Entity("id6", "stockholm"));
        categorization = new Metadata(subject, person, company, location);


    }

    public void testShouldAddCategoriesFromSubjectDimensionAsCommaSeparatedString() throws Exception
    {
        when(lastContent.getChildPolicy("categorization"))
        .thenReturn(categorizationProviderPolicy);

        when(categorizationProviderPolicy.getMetadata())
        .thenReturn(categorization);

        target.addLastContentCategoriesToLocalModel(m, lastContent);

        verifyLocalModel();
    }

    public void testShouldUsePolicyAsCategorizationProvider() throws Exception {
        when(categorizationProviderPolicy.getMetadata())
        .thenReturn(categorization);

        target.addLastContentCategoriesToLocalModel(m, categorizationProviderPolicy);
        
        verifyLocalModel();
    }

    private void verifyLocalModel() {
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("chemistry"));
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("politics"));
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("emma grey"));
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("tom tom"));
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("atex"));
        verify(localModel).setAttribute(org.mockito.Matchers.eq("keywords"), org.mockito.Matchers.contains("stockholm"));
        
        verify(localModel).setAttribute(org.mockito.Matchers.eq("locations"), org.mockito.Matchers.contains("stockholm"));
    }

}
