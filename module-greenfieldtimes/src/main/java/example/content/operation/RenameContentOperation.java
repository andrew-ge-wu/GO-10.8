package example.content.operation;

import java.io.IOException;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.orchid.widget.OBatchOperation;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.ContentOperation;
import com.polopoly.cm.app.orchid.widget.OBatchOperation.OperationFailedException;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.Creator;
import com.polopoly.cm.client.OperationFailureException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.orchid.widget.OWidgetBase;

/**
 * This operation class is an example of how to write a pluggable operation
 * for the {@link OBatchOperation} widget. This particular operation renames all
 * selected content, which may not be the most useful operation of all.
 * Operations must implement {@link ContentOperation} inteface. Since this
 * class also implements the {@link OWidget} interface, it will be initialized
 * and rendered by the {@link OBatchOperation} widget. To create an input
 * template with this batch operation, import the following XML:
 * <pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;template-definition version="1.0" xmlns="http://www.polopoly.com/polopoly/cm/app/xml"&gt;
  &lt;input-template name="example.content.operation.RenameContentOperation"&gt;
    &lt;viewer&gt;com.polopoly.cm.app.widget.OTopPolicyWidget&lt;/viewer&gt;
    &lt;editor&gt;com.polopoly.cm.app.widget.OTopPolicyWidget&lt;/editor&gt;
    &lt;field name="batchoperation" input-template="p.BatchOperation"&gt;
      &lt;param name="operationClassName"&gt;example.content.operation.RenameContentOperation&lt;/param&gt;
    &lt;/field&gt;
  &lt;/input-template&gt;
&lt;/template-definition&gt;
 * </pre>
 * To create a content using this template, import the following XML:
 * <pre>
&lt;?xml version="1.0" ?&gt;
&lt;batch xmlns="http://www.polopoly.com/polopoly/cm/xmlio"&gt;
  &lt;content&gt;
    &lt;metadata&gt;
      &lt;contentid&gt;
        &lt;major&gt;Article&lt;/major&gt;
        &lt;externalid&gt;RenameContentOperation&lt;/externalid&gt;
      &lt;/contentid&gt;
      &lt;input-template&gt;
        &lt;externalid&gt;example.content.operation.RenameContentOperation&lt;/externalid&gt;
      &lt;/input-template&gt;
      &lt;security-parent&gt;
        &lt;externalid&gt;GreenfieldTimes.d&lt;/externalid&gt;
      &lt;/security-parent&gt;
    &lt;/metadata&gt;
    &lt;component group="polopoly.Content" name="name"&gt;Rename Content&lt;/component&gt;
    &lt;contentref name="insertParentId" group="polopoly.Parent"&gt;
      &lt;contentid&gt;
        &lt;externalid&gt;GreenfieldTimes.d&lt;/externalid&gt;
      &lt;/contentid&gt;
    &lt;/contentref&gt;
  &lt;/content&gt;
&lt;/batch&gt;
 * </pre>
 */
public class RenameContentOperation
    extends OWidgetBase implements ContentOperation
{
    private static final String OPERATION_NAME =
        "example.content.operation.RenameContentOperation.name";
    private static final String USER_ERROR_MESSAGE =
        "example.content.operation.RenameContentOperation.error";

    private static final long serialVersionUID = 1L;

    private OTextInput _contentNameInput;

    @Override
    public void initSelf(OrchidContext oc) throws OrchidException
    {
        super.initSelf(oc);
        _contentNameInput = new OTextInput();
        addAndInitChild(oc, _contentNameInput);
    }

    @Override
    public void localRender(OrchidContext oc) throws IOException,
                    OrchidException
    {
        Device device = oc.getDevice();
        device.println("<fieldset><span class=\"legend\">New name for content</span>");
        _contentNameInput.render(oc);
        device.println("</fieldset>");
    }

    public void execute(CmClient cmClient, ContentId id) throws OperationFailedException
    {
        
        try {
            ContentId unversionedId = id.getOtherVersionId(VersionedContentId.UNVERSIONED_VERSION);
            CMServer cmServer = cmClient.getCMServer();
            if(cmServer.getLockInfo(unversionedId) != null){
                throw new OperationFailureException("Content was locked");
            }
            String newName = _contentNameInput.getValue();
            Content content =
                Creator.createLockedContentVersion(cmServer,
                                                   id.getLatestCommittedVersionId());
            content.setName(newName);
            content.commit();
        } catch (Exception e) {
            throw new OperationFailedException("Failed to rename content",
                                               USER_ERROR_MESSAGE, e);
        }
    }

    public String getName()
    {
        return OPERATION_NAME;
    }
}
