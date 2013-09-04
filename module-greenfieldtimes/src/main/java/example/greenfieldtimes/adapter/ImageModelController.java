package example.greenfieldtimes.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.imagemanager.ImageManagerPolicy;
import com.polopoly.cm.app.imagemanager.ImageProvider;
import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.data.AbstractCustomModelController;
import com.polopoly.data.CustomModelContext;
import com.polopoly.data.exception.CustomModelException;
import com.polopoly.data.service.AuthenticationUtil;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.model.PojoAsModel;
import com.polopoly.model.pojo.FileReference;
import com.polopoly.siteengine.standard.image.ImageResource;
import com.polopoly.user.server.Caller;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ImageModelController extends AbstractCustomModelController
{
    private static final Logger LOG = Logger.getLogger(ImageModelController.class.getName());

    /**
     * Returns a model representation of the image using a wrapped ImageBean.
     * @returns a model representation of the image.
     */
    @Override
    public Model getCustomModel(CustomModelContext context, ContentId contentId)
        throws CustomModelException,
               CMException
    {
        Model imageModel = context.getModelDomain().getModel(contentId);

        context.getModelMapper(imageModel.getModelType().getName(), ImageBean.class.getName());
        ImageBean imageBean = new ImageBean();
        imageBean.setName(ModelPathUtil.get(imageModel, "name", String.class));
        imageBean.setDescription(ModelPathUtil.get(imageModel, "description/value", String.class));
        String imagePath = getImagePath(imageModel);
        if (imagePath != null) {
            imageBean.setImage(new FileReference(imagePath));
        }
        imageBean.setByline(ModelPathUtil.get(imageModel, "byline/value", String.class));
        imageBean.setContact(ModelPathUtil.get(imageModel, "contact/value", String.class));
        imageBean.setLicenseUrl(ModelPathUtil.get(imageModel, "licenseurl/value", String.class));

        return new PojoAsModel(context.getModelDomain(), imageBean);
    }

    /**
     * Creates a new version of the content and populates it with data form the incoming model.
     */
    @Override
    public VersionedContentId updateFromCustomModel(CustomModelContext context,
                                                    Model model,
                                                    ContentId contentId)
        throws CustomModelException,
               CMException
    {
        Object bean = PojoAsModel.unwrapModelIfPossible(model);
        if (!(bean instanceof ImageBean)) {
            super.updateFromCustomModel(context, model, contentId);
        }
        ImageBean imageBean = (ImageBean) bean;
        VersionedContentId vid;
        if (contentId instanceof VersionedContentId) {
            vid = (VersionedContentId) contentId;
        } else {
            vid = new VersionedContentId(contentId, VersionedContentId.LATEST_COMMITTED_VERSION);
        }
        Policy policy = context.getPolicyCMServer().createContentVersion(vid);
        return updateImage(context.getPolicyCMServer(), context.getModelDomain(), policy, imageBean);
    }

    /**
     * Creates a new example.Image content from the incoming model.
     */
    @Override
    public VersionedContentId createFromCustomModel(CustomModelContext context, Model model)
            throws CustomModelException, CMException
    {
        Object bean = PojoAsModel.unwrapModelIfPossible(model);
        if (!(bean instanceof ImageBean)) {
            super.createFromCustomModel(context, model);
        }
        ImageBean imageBean = (ImageBean) bean;
        Policy policy = context.getPolicyCMServer().createContent(context.getPolicyCMServer().getMajorByName(DefaultMajorNames.ARTICLE),
                                                                  new ExternalContentId("GreenfieldTimes.d"),
                                                                  new ExternalContentId("example.Image"));
        return updateImage(context.getPolicyCMServer(), context.getModelDomain(), policy, imageBean);
    }

    private VersionedContentId updateImage(PolicyCMServer server, PolicyModelDomain domain, Policy policy, ImageBean imageBean)
            throws CustomModelException, CMException
    {
        try {
            ModelWrite imageModel = (ModelWrite) domain.getModel(policy);
            ModelPathUtil.set(imageModel, "name", imageBean.getName());
            ModelPathUtil.set(imageModel, "description/value", imageBean.getDescription());
            ModelPathUtil.set(imageModel, "byline/value", imageBean.getByline());
            ModelPathUtil.set(imageModel, "contact/value", imageBean.getContact());
            ModelPathUtil.set(imageModel, "licenseurl/value", imageBean.getLicenseUrl());

            String imagePath = getImagePath(imageModel);
            updateImageData(imageModel, imageBean.getImage(), imagePath);
            server.commitContent(policy);
        } catch (RuntimeException e) {
            server.abortContent(policy);
            throw e;
        } catch (CMException e) {
            server.abortContent(policy);
            throw e;
        } catch (IOException e) {
            server.abortContent(policy);
            throw new CustomModelException(e);
        } catch (ImageFormatException e) {
            server.abortContent(policy);
            throw new CustomModelException(e);
        } catch (ImageTooBigException e) {
            server.abortContent(policy);
            throw new CustomModelException(e);
        }

        return policy.getContentId();
    }
    private void updateImageData(Model imageModel, FileReference image, String currentPath)
            throws CMException, IOException, ImageFormatException, ImageTooBigException, CustomModelException
    {
        Object bean = ModelPathUtil.getBean(imageModel);
        ImageManagerPolicy imageManager = null;
        // Figure out where the image manager is
        if (bean instanceof ImageManagerPolicy) {
            imageManager = (ImageManagerPolicy) bean;
        } else {
            Object imageTypeBean = ((Policy) bean).getChildPolicy("imageType");
            if (imageTypeBean instanceof SelectableSubFieldPolicy) {
                SelectableSubFieldPolicy imageType = (SelectableSubFieldPolicy) imageTypeBean;
                if (imageType.getSelectedSubFieldName() == null || !"image".equals(imageType.getSelectedSubFieldName())) {
                    // Don't bother changing selected type when no import is to be performed
                    if (noFile(image)) {
                        return;
                    }
                    Policy potentialImageManager = imageType.getChildPolicy("image");
                    if (potentialImageManager instanceof ImageManagerPolicy) {
                        imageType.setSelectedSubFieldName("image");
                        imageManager = (ImageManagerPolicy) potentialImageManager;
                    }
                } else {
                    Policy potentialImageManager = imageType.getChildPolicy("image");
                    if (potentialImageManager instanceof ImageManagerPolicy) {
                        imageManager = (ImageManagerPolicy) potentialImageManager;
                    }
                }
            }
        }
        if (imageManager == null) {
            Policy policy = (Policy) bean;
            throw new CustomModelException(policy.getContentId().getContentIdString() + ", " +
                    policy.getInputTemplate().getExternalId().getExternalId() + " is not an image manager");
        }
        if (currentPath == null) {
            if (noFile(image)) {
                // Nothing selected and nothing should be selected, ok
                return;
            }
            // Import new image
            FileStream data = getStreamForPath(image.getUrl(), imageManager.getCMServer().getCurrentCaller());
            try {
                String importImage = imageManager.importImage(data.fileName, data.stream);
                imageManager.setSelectedImage(importImage);
            } finally {
                data.stream.close();
            }
            return;
        }
        if (image != null && currentPath.equals(image.getFilePath())
            && image.getUrl() == null)
        {
            // No new image data.
            return;
        }
        if (noFile(image)) {
            // Remove old image
            imageManager.setSelectedImage(null);
            return;
        }
        // Overwrite old image
        FileStream data = getStreamForPath(image.getUrl(), imageManager.getCMServer().getCurrentCaller());
        try {
            String importImage = imageManager.importImage(data.fileName, data.stream);
            imageManager.setSelectedImage(importImage);
        } finally {
            data.stream.close();
        }
    }

    private boolean noFile(FileReference image) {
        if (image == null) {
            return true;
        }
        if (image.getUrl() == null) {
            return true;
        }
        return image.getUrl().length() == 0;
    }

    private static class FileStream {
        public final String fileName;
        public final InputStream stream;
        public FileStream(String fileName, InputStream stream) {
            this.fileName = fileName;
            this.stream = stream;
        }
    }

    private FileStream getStreamForPath(String filePath, Caller caller)
        throws CustomModelException
    {
        WebResource resource = Client.create().resource(filePath);
        String token = AuthenticationUtil.getAuthToken(caller);
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
        String type = resp.getHeaders().getFirst("Content-Type");
        String path;
        if (type == null) {
            path = "image.jpg";
        } else {
            MediaType mt = MediaType.valueOf(type);
            if ("jpeg".equals(mt.getSubtype())) {
                path = "image.jpg";
            } else if (!"*".equals(mt.getSubtype())) {
                path = "image." + mt.getSubtype();
            } else {
                path = "image.jpg";
            }
        }
        return new FileStream(path, resp.getEntityInputStream());
    }

    private String getImagePath(Model imageModel) throws CMException
    {
        Object bean = ModelPathUtil.getBean(imageModel);
        ImageSet selectedImage = null;
        if (bean instanceof ImageResource) {
            selectedImage = ((ImageResource) bean).getImageSet();
        }
        else {
            ImageProvider imageProvider = (ImageProvider) ModelPathUtil.getBean(imageModel, "imageType/selected");
            if (imageProvider != null) {
                selectedImage = imageProvider.getSelectedImage();
            }
        }

        if (selectedImage != null) {
            String imagePath = null;
            try {
                return selectedImage.getImage().getPath();
            } catch (IOException e) {
                String logMessage = String.format("Unable to get image in content using path: '%s'.", imagePath);
                LOG.log(Level.WARNING, logMessage, e);
            }
        }

        return null;
    }
}
