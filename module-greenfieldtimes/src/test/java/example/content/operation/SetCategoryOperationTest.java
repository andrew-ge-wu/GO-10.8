package example.content.operation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.LockInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.OperationFailedException;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.policy.MetadataAwarePolicy;

import example.MockitoBase;
import example.content.operation.SetCategoryOperation.SetCategoryWithPreservedWorkflowState;

public class SetCategoryOperationTest extends MockitoBase {

    private SetCategoryOperation target;
    private ContentId contentId;
    @Mock private CmClient cmClient;
    @Mock private CMServer cmServer;
    @Mock private PolicyCMServer policyCmServer;
    @Mock private LockInfo lockInfo;
    @Mock private Content content;
    @Mock private OPolicyWidget categorySelect;
    @Mock private MetadataAwarePolicy sourcePolicy;
    @Mock private MetadataAwarePolicy targetPolicy;
    private Metadata selectedCategorization;
    private VersionedContentId defaultStageVersion;
    private VersionedContentId defaultStageVersionId;
    private VersionedContentId symbolicLatestCommittedVersion;
    private VersionedContentId newVersion;
    private VersionedContentId unversionedId;
    private Metadata initialCategorization;
    private Metadata newCategorization;
    @Mock MetadataAwarePolicy categorizationProviderPolicy;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        target = new SetCategoryOperation();
        contentId = new ContentId(1, 111);
        Dimension originalAbcDimension = new Dimension("abc", "abc", false, new Entity("bla"));
        Dimension originalCdeDimension = new Dimension("cde", "cde", false, new Entity("ble"));
        Dimension newAbcDimension = new Dimension("abc", "abc", false, new Entity("gny"));
        initialCategorization = new Metadata(originalAbcDimension, originalCdeDimension);
        selectedCategorization = new Metadata(newAbcDimension);
        newCategorization = new Metadata(newAbcDimension, originalCdeDimension);

        target._categorySelect = categorySelect;
        defaultStageVersion = new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION);
        defaultStageVersionId = new VersionedContentId(1, 111, -1001);
        symbolicLatestCommittedVersion = new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION);
        newVersion = new VersionedContentId(defaultStageVersionId, VersionedContentId.NEW_VERSION);
        unversionedId = new VersionedContentId(1, 111, VersionedContentId.UNVERSIONED_VERSION);
        
        when(cmClient.getCMServer()).thenReturn(cmServer);
        when(cmClient.getPolicyCMServer()).thenReturn(policyCmServer);
        when(policyCmServer.getPolicyFor(content)).thenReturn(targetPolicy);
        
        when(cmServer.translateSymbolicContentId(defaultStageVersionId)).
            thenReturn(defaultStageVersion);
        
        when(cmServer.translateSymbolicContentId(symbolicLatestCommittedVersion)).
        thenReturn(new VersionedContentId(1, 111, VersionedContentId.LATEST_COMMITTED_VERSION));
    
      
        when(cmServer.createLockedContents(new VersionedContentId[]{newVersion}, 
                                       new VersionedContentId[]{defaultStageVersion}, 
                                       null)).thenReturn(new Content[]{content});
    
        when(categorySelect.getPolicy()).thenReturn(sourcePolicy);
        when(sourcePolicy.getMetadata()).thenReturn(selectedCategorization);
    
        when(targetPolicy.getChildPolicy("categorization")).thenReturn(targetPolicy);
        when(targetPolicy.getMetadata()).thenReturn(initialCategorization);
    
        when(categorizationProviderPolicy.getMetadata()).thenReturn(initialCategorization);
    }
    
    public void testStealingLockedContentShouldFail() throws Exception {     
        when(cmServer.getLockInfo(unversionedId)).thenReturn(lockInfo);

        try {
            //execute should give exception
            target.execute(cmClient, contentId);
            fail();
        } catch (OperationFailedException e) {
            //expected
        }
    }

    public void testTakingUnlockedContentShouldSucceed() throws Exception {        
        when(cmServer.getLockInfo(unversionedId)).thenReturn(null);
      //execute should give exception
        target.execute(cmClient, contentId);  
    }

    public void testShouldNotOverWriteUnselectedCategories() throws Exception {
        SetCategoryWithPreservedWorkflowState target = 
            new SetCategoryOperation.SetCategoryWithPreservedWorkflowState(contentId, cmClient, categorySelect);
        target.contentOperation(content);
        verify(targetPolicy).setMetadata(newCategorization);
    }
    
    public void testShouldUseCategorizationProviderPolicyToSetCategorization() throws Exception {
        when(policyCmServer.getPolicyFor(content)).thenReturn(categorizationProviderPolicy);
        SetCategoryWithPreservedWorkflowState target = 
            new SetCategoryOperation.SetCategoryWithPreservedWorkflowState(contentId, cmClient, categorySelect);
        target.contentOperation(content);
        verify(categorizationProviderPolicy).setMetadata(newCategorization);
    }
}
