package example.data;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.data.CustomModelController;
import com.polopoly.data.service.DataApiUtil;
import com.polopoly.data.service.ErrorResponseException;
import com.polopoly.model.Model;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * A simple JAX-RS implementation of a custom data API. Works a lot like the standard data API, but instead of checking CM
 * permissions it checks only that the content belongs to the active site. This is intended to be used for the same
 * kind of content that would be available using Site Engine, i.e. if it has a controller (or output template, in Site
 * Engine) and is made public, it is available on the web.
 */
@Path("content")
@PerRequest
public class CustomDataResource {
    /**
     * DataApiUtil needs to be set up per request, which is why this class must be annotated with @PerRequest.
     */
    DataApiUtil dataApiUtil;

    /**
     * Creates an instance with a DataApiUtil. Will be called by the JAX-RS implementation (e.g. Jersey).
     *
     * @param servletContext injected by Jersey
     * @param httpHeaders injected by Jersey
     * @param uriInfo inject by Jersey
     */
    public CustomDataResource(@Context ServletContext servletContext,
                              @Context ServletConfig servletConfig,
                              @Context HttpHeaders httpHeaders,
                              @Context UriInfo uriInfo)
    {
        dataApiUtil = new DataApiUtil(servletContext, servletConfig, httpHeaders, uriInfo);
    }

    @GET
    @Path("contentid/{id}")
    @Produces({"application/xml; charset=utf-8", "application/json; charset=utf-8"})
    public Response getContentWithContentId(@PathParam("id") String id)
        throws ErrorResponseException
    {
        ContentId contentId = dataApiUtil.parseId(id);

        if (dataApiUtil.etagMatched(contentId)) {
            return dataApiUtil.notModified(contentId);
        } else if (contentId.isSymbolicId()) {
            // To facilitate caching of the actual content, redirect to a
            // URL with a versioned content ID.
            return dataApiUtil.forwardTo(contentId);
        }

        if (!checkPermission(contentId)) {
            throw dataApiUtil.errorForbidden("Permission denied.");
        }

        CustomModelController ctrl = dataApiUtil.mapRead((VersionedContentId) contentId);
        Model model = dataApiUtil.get(ctrl, (VersionedContentId) contentId);
        return dataApiUtil.responseWithData((VersionedContentId) contentId, model);
    }

    @GET
    @Path("externalid/{id}")
    @Produces({"application/xml; charset=utf-8", "application/json; charset=utf-8"})
    public Response getContentWithExternalId(@PathParam("id") String id)
        throws ErrorResponseException
    {
        ExternalContentId eid = new ExternalContentId(id);

        if (!checkPermission(eid)) {
            throw dataApiUtil.errorForbidden("Permission denied");
        }

        if (dataApiUtil.etagMatched(eid)) {
            return dataApiUtil.notModified(eid);
        }

        // To facilitate caching of the actual content, redirect to a
        // URL with a versioned content ID.
        return dataApiUtil.forwardTo(eid);
    }

    /**
     * We have no permissions check here, because content will only be available if
     * it is public (enforced by the CM client) and has a variant mapping
     * (enforced by DataApiUtil), which is good enough for most use cases. It is present here
     * mostly to demonstrate that you could add explicit permission checking if you do need it,
     * and also to make it clear that we do not do ACL permission checking by default,
     * because it is very expensive and not suitable for stuff available to the public.
     */
    private boolean checkPermission(ContentId contentId)
        throws ErrorResponseException
    {
        return true;
    }
}

