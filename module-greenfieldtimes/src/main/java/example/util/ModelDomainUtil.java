package example.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.model.Model;
import com.polopoly.siteengine.dispatcher.SiteEngine;

/**
 * Utilities for working with model domains.
 * 
 */
public class ModelDomainUtil {

    private static final Logger logger = Logger.getLogger(ModelDomainUtil.class.getName());

    /**
     * Returns a model for a given content id.
     * 
     * @param contentId
     *                the content id
     * @return a model for the given content id.
     */
    public Model getModel(ContentId contentId) throws CMException {

        PolicyModelDomain policyModelDomain =
            (PolicyModelDomain) SiteEngine.getApplication() /* new_wiring_approved */
                                          .getModelDomain();

        return policyModelDomain.getModel(contentId);
    }

    /**
     * Returns a model for given content id string.
     * 
     * @param contentIdString
     *                the content id string
     * @return a model for the given content id.
     * @throws CMException
     */
    public Model getModel(String contentIdString) throws CMException
    {
        try {
            return getModel(ContentIdFactory.createContentId(contentIdString));
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Couldn't get model for given content id string.", e);
        }
        return null;
    }
}
