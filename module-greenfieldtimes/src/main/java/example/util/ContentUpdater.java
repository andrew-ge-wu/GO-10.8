package example.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.HttpCmClientHelper;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserServer;

/**
 * Program that updates content using a specific template.
 * Usage: java ContentUpdater connection-url username password templateid [action]
 * 
 */
public class ContentUpdater {
    protected static final String CLASS = ContentUpdater.class.getName();

    public static void main(String[] args)
    {
        if (args.length < 4) {
            System.err.println("Wrong number of arguments\nUsage: java " + CLASS
                    + " config-file username password templateid [action]");
            System.exit(0);
        }

        String connectionURL = args[0];
        String userName = args[1];
        String password = args[2];

        Application app = new StandardApplication("updater");

        UserServer userServer = null;
        PolicyCMServer cmServer = null;

        try {
            // Create and apply connection properties.
            ConnectionProperties connectionProperties =
                    new ConnectionProperties(new URL(connectionURL));
            CmClient cmClient = HttpCmClientHelper.createAndAddToApplication(app, connectionProperties);
            app.readConnectionProperties(connectionProperties);
            app.init();

            userServer = cmClient.getUserServer();
            cmServer = cmClient.getPolicyCMServer();

            Caller caller = userServer.loginAndMerge(userName, password, null);
            cmServer.setCurrentCaller(caller);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not get servers. Exiting.");
            System.exit(0);
        }

        // Get template name
        String templateName = args[3];

        // Check if is update
        boolean isUpdate = false;
        if (args.length > 4 && "update".equals(args[4])) {
            isUpdate = true;
        }

        // Find template id
        ContentId templateId = null;
        try {
            templateId = cmServer.findContentIdByExternalId(new ExternalContentId(templateName));
        } catch (CMException e) {
            e.printStackTrace();
        }
        if (templateId == null) {
            System.out.println("Could not find template with external id " + templateName);
            System.exit(0);
        }

        // Find contents
        ContentId[] contentIds = null;
        try {
            contentIds =
                    cmServer.getReferringContentIds(templateId,
                            VersionedContentId.LATEST_COMMITTED_VERSION, "polopoly.Content",
                            "inputTemplateId");
            System.out.println("Found " + (contentIds == null ? 0 : contentIds.length)
                    + " contents using " + templateName + ".");
        } catch (CMException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // Return if is only view
        if (!isUpdate) {
            System.exit(0);
        }

        // Update contents
        List<ContentId> failedContentIds = new ArrayList<ContentId>();
        int nofCommittedContentIds = 0;
        for (int i = 0; contentIds != null && i < contentIds.length; i++) {
            try {
                System.out.println("Updating " + contentIds[i].getContentId().getContentIdString());
                Policy newVersion =
                        cmServer.createContentVersion(new VersionedContentId(contentIds[i],
                                VersionedContentId.LATEST_COMMITTED_VERSION));
                cmServer.commitContent(newVersion);

                nofCommittedContentIds++;
            } catch (CMException e) {
                failedContentIds.add(contentIds[i]);
                e.printStackTrace();
            }
        }

        System.out.println("Updated " + nofCommittedContentIds + " contents.");

        if (failedContentIds.size() > 0) {
            System.out.println("Failed updateing some contents. These are:");
            for (Iterator<ContentId> iterator = failedContentIds.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());
            }
        }
    }
}
