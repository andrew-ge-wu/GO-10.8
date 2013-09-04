package example.content.image;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.imagemanager.HttpImageManager;
import com.polopoly.cm.app.imagemanager.ImageFormatException;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.util.WidgetUtil;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextInput;
import com.polopoly.util.LocaleUtil;

public class OHttpImageUrl extends OFieldPolicyWidget
    implements Editor, Viewer
{
    private static final long serialVersionUID = 2885926543857375353L;
    
    private HttpImageManager policy;
    private OTextInput textInput;
    private OSubmitButton updateButton;
    private URL oldUrl;
    
    private static final Logger LOG = Logger.getLogger(OHttpImageUrl.class.getName());

    @Override
    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);
        
        policy = (HttpImageManager) getParentPolicyWidget().getPolicy();

        textInput = new OTextInput();
        textInput.setSize(50);
        
        URL url;
        
        try {
            url = policy.getUrl();
            oldUrl = url;
        } catch (MalformedURLException e) {
            throw new OrchidException("failed to get url", e);
        } catch (CMException e) {
            throw new OrchidException("failed to get url", e);
        }

        String urlText = url != null ? url.toString() : "";
        textInput.setText(urlText);
        textInput.setEditable(PolicyWidgetUtil.isEditMode(this));
        
        addAndInitChild(oc, textInput);

        updateButton = new OSubmitButton();
        updateButton.setLabel(LocaleUtil.format("cm.action.Update", oc.getMessageBundle()));
        updateButton.setEnabled(PolicyWidgetUtil.isEditMode(this));
        
        addAndInitChild(oc, updateButton);

        updateButton.addSubmitListener(new WidgetEventListener() {
            public void processEvent(OrchidContext oc, OrchidEvent event) throws OrchidException {
                updateImage(oc);
            }
        });
    }

    /*
     * Since this widget doesn't map to a real policy, it doesn't need to do
     * any validation - the image manager is responsible for validation.  All
     * the error handling needed here is done in the postDecode step.
     */
    @Override
    public PrepareResult validate()
        throws CMException
    {
        return null;
    }

    @Override
    public void storeSelf()
        throws CMException
    {
        updateImage(WidgetUtil.getOrchidContext());
    }

    private void updateImage(OrchidContext oc)
    {
        try {
            URL url = new URL(textInput.getValue());
            
            if (oldUrl == null || !oldUrl.equals(url)) {
                policy.deleteImage();
                policy.updateImage(url);
                oldUrl = url;
            }
        } catch (MalformedURLException e) {
            LOG.log(Level.FINE, "Not a valid URL.", e);
            handleError(oc, LocaleUtil.format("cm.msg.NotAValidURL", oc.getMessageBundle()));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to read image.", e);
            handleError(oc, LocaleUtil.format("cm.msg.FailedToReadImage", oc.getMessageBundle()));
        } catch (ImageFormatException e) {
            LOG.log(Level.FINE, "Unsupported image format.", e);
            handleError(oc, LocaleUtil.format("cm.msg.NotSupportedImageFormat", oc.getMessageBundle()));
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unexpected error.", e);
            handleError(oc, LocaleUtil.format("cm.msg.UnexpectedError", oc.getMessageBundle()));
        }
    }
}
