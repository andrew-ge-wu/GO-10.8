package example.greenfieldtimes.adapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.FilePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.data.AbstractCustomModelController;
import com.polopoly.data.CustomModelContext;
import com.polopoly.data.exception.CustomModelException;
import com.polopoly.data.service.AuthenticationUtil;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.model.PojoAsModel;
import com.polopoly.model.pojo.FileReference;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import example.content.file.FileResourcePolicy;

public class FileModelController extends AbstractCustomModelController
{
    private static final Logger LOG = Logger.getLogger(FileModelController.class.getName());

    /**
     * Returns a model representation of the file using a wrapped FileBean.
     * @returns a model representation of the file.
     */
    @Override
    public Model getCustomModel(CustomModelContext context, ContentId contentId)
        throws CustomModelException, CMException
    {
        Model fileModel = context.getModelDomain().getModel(contentId);
        FileBean fileBean = new FileBean();
        fileBean.setName(ModelPathUtil.get(fileModel, "name", String.class));
        fileBean.setDescription(ModelPathUtil.get(fileModel, "lead/value", String.class));
        fileBean.setByline(ModelPathUtil.get(fileModel, "byline/value", String.class));
        fileBean.setContact(ModelPathUtil.get(fileModel, "contact/value", String.class));
        fileBean.setLicenseUrl(ModelPathUtil.get(fileModel, "licenseurl/value", String.class));

        FilePolicy filePolicy = (FilePolicy) ModelPathUtil.getBean(fileModel, "file");
        fileBean.setFile(new FileReference(filePolicy.getFullFilePath()));

        return new PojoAsModel(context.getModelDomain(), fileBean);
    }

    /**
     * Creates a new example.File content from the incoming model.
     */
    @Override
    public VersionedContentId createFromCustomModel(CustomModelContext context,
                                                    Model model)
        throws CustomModelException, CMException
    {
        Object bean = PojoAsModel.unwrapModelIfPossible(model);
        if (!(bean instanceof FileBean)) {
            super.createFromCustomModel(context, model);
        }
        FileResourcePolicy fileResourcePolicy =
                (FileResourcePolicy) context.getPolicyCMServer().createContent(context.getPolicyCMServer().getMajorByName(DefaultMajorNames.ARTICLE),
                                                                               new ExternalContentId("GreenfieldTimes.d"),
                                                                               new ExternalContentId("example.File"));
        FileBean fileBean = (FileBean) bean;
        try {
            populateModelFromBean(fileResourcePolicy, fileBean, context);
            storeFileInContent(fileResourcePolicy, fileBean, context);
            context.getPolicyCMServer().commitContent(fileResourcePolicy);
        } catch (Exception e) {
            context.getPolicyCMServer().abortContent(fileResourcePolicy);
            throw new CustomModelException(e);
        }

        return fileResourcePolicy.getContentId();
    }

    /**
     * Creates a new version of the content and populates it with data form the incoming model.
     */
    @Override
    public VersionedContentId updateFromCustomModel(CustomModelContext context,
                                                    Model model,
                                                    ContentId contentId)
        throws CustomModelException, CMException
    {
        Object bean = PojoAsModel.unwrapModelIfPossible(model);
        if (!(bean instanceof FileBean)) {
            super.updateFromCustomModel(context, model, contentId);
        }
        VersionedContentId vid;
        if (contentId instanceof VersionedContentId) {
            vid = (VersionedContentId) contentId;
        } else {
            vid = new VersionedContentId(contentId, VersionedContentId.LATEST_COMMITTED_VERSION);
        }
        FileResourcePolicy fileResourcePolicy =
                (FileResourcePolicy) context.getPolicyCMServer().createContentVersion(vid);
        FileBean fileBean = (FileBean) bean;
        try {
            populateModelFromBean(fileResourcePolicy, fileBean, context);
            storeFileInContent(fileResourcePolicy, fileBean, context);
            context.getPolicyCMServer().commitContent(fileResourcePolicy);
        } catch (Exception e) {
            context.getPolicyCMServer().abortContent(fileResourcePolicy);
            throw new CustomModelException(e);
        }

        return fileResourcePolicy.getContentId();
    }

    private void populateModelFromBean(FileResourcePolicy fileResourcePolicy,
                                       FileBean fileBean,
                                       CustomModelContext context)
        throws CMException
    {
        ModelWrite fileModel = (ModelWrite) context.getModelDomain().getModel(fileResourcePolicy);
        ModelPathUtil.set(fileModel, "name", fileBean.getName());
        ModelPathUtil.set(fileModel, "lead/value", fileBean.getDescription());
        ModelPathUtil.set(fileModel, "byline/value", fileBean.getByline());
        ModelPathUtil.set(fileModel, "contact/value", fileBean.getContact());
        ModelPathUtil.set(fileModel, "licenseurl/value", fileBean.getLicenseUrl());
    }

    private void storeFileInContent(FileResourcePolicy fileResourcePolicy,
                                    FileBean fileBean,
                                    CustomModelContext context)
        throws CMException,
               CustomModelException
    {
        FilePolicy filePolicy = fileResourcePolicy.getFilePolicy();
        FileReference fileReference = fileBean.getFile();
        InputStream data = getInputStreamFromUrl(fileBean, context);
        if (fileReference.getUrl() != null) {
            filePolicy.setFullFilePath(fileReference.getFilePath());
            try {
                int lastSlashIndex = fileReference.getFilePath().lastIndexOf("/");
                if (lastSlashIndex >= 0) {
                    String directoryPath = fileReference.getFilePath().substring(0, lastSlashIndex);
                    try {
                        filePolicy.getFileInfo(directoryPath);
                    } catch (FileNotFoundException createDiretories) {
                        LOG.log(Level.FINEST, "Creating directory: " + directoryPath);
                        filePolicy.createDirectory(directoryPath, true);
                    }
                }
                filePolicy.importFile(fileReference.getFilePath(), data);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Unable to import file: '" + fileReference.getUrl()
                                       + "' in content: '" + fileResourcePolicy.getContentId() + "'.",
                        e);
                throw new CustomModelException(e);
            } finally {
                if (data != null) {
                    try {
                        data.close();
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "Unable to close stream for: '" + fileReference.getUrl()
                                               + "' in content: '" + fileResourcePolicy.getContentId() + "'.",
                                e);
                    }
                }
            }
        }
    }

    private InputStream getInputStreamFromUrl(FileBean fileBean,
                                              CustomModelContext context)
        throws CustomModelException
    {
        FileReference file = fileBean.getFile();
        WebResource resource = Client.create().resource(file.getUrl());
        String token = AuthenticationUtil.getAuthToken(context.getPolicyCMServer().getCurrentCaller());
        ClientResponse resp;
        try {
            resp = resource.header("X-Auth-Token", token).get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            if (e.getCause() instanceof ConnectException) {
                throw new CustomModelException(e.getCause());
            } else {
                throw e;
            }
        }
        return resp.getEntityInputStream();
    }
}
