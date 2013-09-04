package example.akismet;

import java.io.IOException;

import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OAbstractPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.orchid.widget.OTextOutput;
import com.polopoly.util.LocaleUtil;

/**
 * Widget for verification of akismet keys.
 */
public class OAkismetKeyVerificationPolicyWidget extends OAbstractPolicyWidget
    implements Viewer, Editor {

    private static final long serialVersionUID = -7294397484814473385L;

    private OTextOutput keyValidityLabel = null;
    private OTextOutput resultOutput = null;
    
    private OSubmitButton verificationButton = null;
    
    private String validKeyMessage = null;
    private String invalidKeyMessage = null;
    private String notValidatedYetMesssage = null;
    private String disabledMessage = null;
    
    private String errorMessage = null;

    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);
        
        // Messages

        validKeyMessage = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.ValidKey", oc.getMessageBundle());
        
        invalidKeyMessage = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.InvalidKey", oc.getMessageBundle());
        
        notValidatedYetMesssage = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.NotValidatedYet", oc.getMessageBundle());

        disabledMessage = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.Disabled", oc.getMessageBundle());
        
        errorMessage = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.KeyVerificationError", oc.getMessageBundle());
        
        // Labels
        
        keyValidityLabel = new OTextOutput(LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.KeyValidity", oc.getMessageBundle()));
        
        addAndInitChild(oc, keyValidityLabel);
        
        resultOutput = new OTextOutput();
        resultOutput.setText(notValidatedYetMesssage);
        addAndInitChild(oc, resultOutput);
        
        verificationButton = new OSubmitButton();
        verificationButton.setLabel(LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.VerifyKey", oc.getMessageBundle()));
        
        addAndInitChild(oc, verificationButton);
        
        verificationButton.addSubmitListener(new WidgetEventListener() {

            public void processEvent(OrchidContext orchidContext, OrchidEvent e)
                throws OrchidException
            {
                try {
                    PolicyWidget topWidget = getContentSession().getTopWidget();
                    
                    if (PolicyWidgetUtil.isEditMode(topWidget)) {
                        // Make sure all values are policy-stored if in edit-mode
                        topWidget.store();
                    }
                    
                    Policy topPolicy = PolicyUtil.getTopPolicy(getPolicy());
                    SingleValued apiKeyPolicy = (SingleValued) topPolicy.getChildPolicy("apiKey");

                    String apiKey = apiKeyPolicy.getValue();
                    
                    if (apiKey != null) {
                        AkismetClient client = AkismetClientFactory.getInstance(
                                orchidContext.getDevice().getServletContext(), false).getClient(apiKey, "http://...");
                        
                        boolean validKey = ((AkismetClientImpl) client).verifyAPIKey();
                        resultOutput.setText((validKey) ? validKeyMessage : invalidKeyMessage);
                    }
                    else {
                        resultOutput.setText(disabledMessage);
                    }
                } catch (CMException cme) {
                    handleError(cme, orchidContext);
                } catch (AkismetException ake) {
                    resultOutput.setText(errorMessage + ": " + ake.getMessage());
                }
            }
            
        });
    }
    
    public void localRender(OrchidContext oc)
        throws OrchidException, IOException
    {
        Device device = oc.getDevice();
        
        String keyValidityLocalized = LocaleUtil.format(
                "cm.template.example.Blog.akismetSettings.KeyValidity", oc.getMessageBundle());
        
        device.println("<b>" + keyValidityLocalized + ": </b>");
        resultOutput.render(oc);
        device.println("<br />");
        
        verificationButton.render(oc);
    }
    
}
