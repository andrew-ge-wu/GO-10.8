package example.content.flash;

import java.io.IOException;

import com.atex.plugins.baseline.url.OrchidUrlResolver;
import com.atex.plugins.baseline.url.UrlResolver;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OComplexFieldPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.util.LocaleUtil;

@SuppressWarnings("serial")
public class OFlashPreviewWidget extends OComplexFieldPolicyWidget
    implements Viewer, Editor
{
    private int _maxWidth;
    private int _maxHeight;

    public void initSelf(OrchidContext oc)
        throws OrchidException
    {
        super.initSelf(oc);
        _maxWidth = PolicyUtil.getParameterAsInt("maxWidth", 512, getPolicy());
        _maxHeight = PolicyUtil.getParameterAsInt("maxHeight", 512, getPolicy());
    }

    /**
     * Make sure other fields store their values.
     */
    public void preRender(OrchidContext oc)
        throws OrchidException
    {
        try {
            if (PolicyWidgetUtil.isEditMode(this)) {
                getContentSession().getTopWidget().store();
            }
        } catch (CMException e) {
            handleError(e, oc);
        }
    }

    public void localRender(OrchidContext oc)
        throws OrchidException, IOException
    {
        Policy resourcePolicy = getContentSession().getTopPolicy();
        if (!(resourcePolicy instanceof FlashResourcePolicy)) {
            throw new OrchidException
                ("'" + resourcePolicy + "' is not a FlashResourcePolicy."
                 + " OFlashWidget can only work on FlashResourcePolicy");
        }
    
        Device device = oc.getDevice();
        UrlResolver urlResolver = new OrchidUrlResolver(oc);
        FlashResourcePolicy resource = (FlashResourcePolicy) resourcePolicy;
        String flashPath = resource.getPreviewPath(urlResolver);
        if (flashPath != null) {
            try {

                // Scale height and width
                String scaleMessage = "";
                int width = resource.getWidth();
                int height = resource.getHeight();
                if (width > _maxWidth || height > _maxHeight) {
                    double widthMod = width / (double) _maxWidth;
                    double heightMod = height / (double) _maxHeight;
                    if (widthMod > heightMod) {
                        width = _maxWidth;
                        height = (int) (height / widthMod);
                    } else {
                        height = _maxHeight;
                        width = (int) (width / heightMod);
                    }
                    scaleMessage =
                        LocaleUtil.format("cm.template.example.Flash.PreviewScaled",
                                          oc.getMessageBundle());
                }
                
                // Display object (flash)  tag
                device.println("<object type='application/x-shockwave-flash'"
                               + " data='" + flashPath + "'"
                               + " width='" + width + "'"
                               + " height='" + height + "'>");
                device.println("<param name='movie' value='" + flashPath + "' />");
                // Print parameters
                String[] pNames = resource.getParameterNames();
                for (int i = 0; i < pNames.length; i++) {
                    String pName = pNames[i];
                    String pValue = resource.getParameterValue(pName);
                    device.println("<param name='" + pName + "'"
                                   + " value='" + pValue + "' />");
                }

                // End tag
                device.println("</object><br/>");
                device.println(scaleMessage);

            } catch (CMException cme) {
                throw new OrchidException
                    ("Unable to render flash object tag", cme);
            }
        }
    }
}
