package example.content;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionInfo;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.OutputTemplate;

public interface ContentBaseModelTypeDescription {

    public VersionedContentId getContentId() throws CMException;

    public VersionInfo getVersionInfo();

    public String getName() throws CMException;

    public void setName(String name) throws CMException;

    public OutputTemplate getOutputTemplate(String mode) throws CMException;

    public ContentId[] getParentIds() throws CMException;
}
