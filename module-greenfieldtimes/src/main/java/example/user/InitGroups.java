package example.user;

import java.net.URL;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.HttpCmClientHelper;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.Acl;
import com.polopoly.user.server.AclEntry;
import com.polopoly.user.server.AclId;
import com.polopoly.user.server.AuthenticationFailureException;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.Group;
import com.polopoly.user.server.GroupId;
import com.polopoly.user.server.InvalidSessionKeyException;
import com.polopoly.user.server.PermissionDeniedException;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserServer;

/**
 * Class used to initialize groups in the Greenfield Times application. Usage: <br />
 * InitGroups <connection properties url> <login> <password>
 */
public class InitGroups {

    private Application _application;
    private PolicyCMServer _cmServer;
    private UserServer _userServer;
    private Caller _caller;

    private static final String GROUP_GT_ADMINS = "Greenfield times administrators";
    private static final String GROUP_GT_EDITORS = "Greenfield times editors";
    private static final String GROUP_GT_MODS = "Greenfield times moderators";
    private static final String GROUP_GH_ADMINS = "Greyhound post administrators";
    private static final String GROUP_GH_EDITORS = "Greyhound post editors";
    private static final String GROUP_GH_MODS = "Greyhound post moderators";

    private static final String[] GROUPS = {
        GROUP_GT_EDITORS, GROUP_GT_ADMINS, GROUP_GT_MODS,
        GROUP_GH_EDITORS, GROUP_GH_ADMINS, GROUP_GH_MODS
    };

    private static final String[] GREENFIELD_TIMES_USERS = { "edmund", "edith", "adrian" };
    private static final String[] GREYHOUND_POST_USERS = { "emma", "eddy", "amber" };

    private static final String[] ADMIN_PERMISSIONS = { "1READ", "1WRITE", "1CREATE", "1REMOVE",
                                                        "2READ", "2WRITE", "2CREATE",
                                                        "7READ", "7WRITE", "7CREATE",
                                                        "13READ", "13WRITE", "13CREATE",
                                                        "18READ", "18WRITE", "18CREATE",
                                                        "19READ", "19WRITE", "19CREATE", "19REMOVE"
    };

    private static final String[] EDITOR_PERMISSIONS = { "1READ", "1WRITE", "1CREATE",
                                                         "2READ", "2WRITE", "2CREATE",
                                                         "7READ", "7WRITE", "7CREATE",
                                                         "13READ", "13WRITE", "13CREATE"
    };

    private static final String[] MODERATOR_PERMISSIONS = { "19WRITE" } ;

    private static final String DEPARTMENT_GREENFIELD_TIMES = "GreenfieldTimes.d";
    private static final String DEPARTMENT_GREYHOUND_POST = "GreyhoundPost.d";

    public static void main(String[] args)
    {
        if (args.length < 3) {
            System.err.println("Usage InitGroups <connection properties url> <login> <passwd>");
            System.exit(1);
        }

        InitGroups initGroups = new InitGroups();
        initGroups.init(args);

        System.exit(0);
    }

    public void init(String[] args)
    {
        try {
            // Init servers
            initServers(args);

            // Create the groups to use
            createGroups();

            // Setting permissions for Greenfield times editors
            setPermissions(DEPARTMENT_GREENFIELD_TIMES, GROUP_GT_EDITORS, EDITOR_PERMISSIONS);

            // Setting permissions for Greenfield times administrators
            setPermissions(DEPARTMENT_GREENFIELD_TIMES, GROUP_GT_ADMINS, ADMIN_PERMISSIONS);

            // Setting permissions for Greenfield times moderators
            setPermissions(DEPARTMENT_GREENFIELD_TIMES, GROUP_GT_MODS, MODERATOR_PERMISSIONS);

            // Setting permissions for Greyhound post editors
            setPermissions(DEPARTMENT_GREYHOUND_POST, GROUP_GH_EDITORS, EDITOR_PERMISSIONS);

            // Setting permissions for Greyhound post administrators
            setPermissions(DEPARTMENT_GREYHOUND_POST, GROUP_GH_ADMINS, ADMIN_PERMISSIONS);

            // Setting permissions for Greyhound post moderators
            setPermissions(DEPARTMENT_GREYHOUND_POST, GROUP_GH_MODS, MODERATOR_PERMISSIONS);

            // Adds users to groups
            addGreenfieldTimesUsers();
            addGreyhoundPostUsers();

            setLoginNameForGreenfieldTimesUser();
        } catch (AuthenticationFailureException e) {
            System.err.println("Unable to login with: " + args[1]
                             + " using password: " + args[2]);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _application.destroy();
            System.exit(1);
        }
    }

    /**
     * Method used to init all servers.
     *
     * @param args
     *                An array containing the path to polopolyclient.properties,
     *                user name, password
     * @throws Exception
     */
    private void initServers(String[] args)
        throws Exception
    {
        ConnectionProperties connectionProperties = new ConnectionProperties(new URL(args[0]));

        _application = new StandardApplication("initGroups");

        CmClient cmClient = HttpCmClientHelper.createAndAddToApplication(_application, connectionProperties);
        _application.addApplicationComponent(cmClient);
        _application.readConnectionProperties(connectionProperties);
        _application.init();

        _cmServer = cmClient.getPolicyCMServer();
        _userServer = cmClient.getUserServer();
        _caller = _userServer.loginAndMerge(args[1], args[2], null);
        _cmServer.setCurrentCaller(_caller);
    }

    /**
     * Method used to set permissions for a content.
     *
     * @param externalId
     *                The external id referring to the content
     * @param groupName
     *                The groupName to set the permission for
     * @param permissions
     *                Which permisions to that should be set
     * @throws CMException
     */
    private void setPermissions(String externalId,
                                String groupName,
                                String[] permissions)
    {
        Content content = null;

        try {
            VersionedContentId cId = _cmServer.findContentIdByExternalId(new ExternalContentId(
                                                                         externalId));

            if (cId != null) {
                content = (Content) _cmServer.getContent(cId);
                // Since we must add an ACL to the contents metadata, we have to
                // lock the content before adding the ACL
                content.lock();

                // Create a new acl and set default permissions for the content

                AclId id = content.getAclId();

                if (id == null) {
                    id = content.createAcl();
                    System.err.println("Creating ACL for content: " + cId);
                } else {
                    System.err.println("Using existing ACL " + id);
                }

                Acl acl = _userServer.findAcl(id);

                // We only want to add an Acl if none were found
                if (acl != null) {

                    AclEntry entry = null;
                    GroupId groupIds[] = _userServer.findGroupsByName(groupName);

                    if (groupIds != null && groupIds.length > 0) {
                        GroupId groupId = groupIds[0];
                        entry = acl.getEntry(groupId);
                        if (entry == null) {
                            entry = new AclEntry(groupId);
                        }
                    }

                    // Add the permissions to the entry
                    if (entry != null) {
                        for (int j = 0; j < permissions.length; j++) {
                            entry.addPermission(permissions[j]);
                            System.err.println("Adding permission: "
                                             + permissions[j]);
                        }
                        // Add the entry to the ACL
                        acl.addEntry(entry, _caller);
                        System.err.println("Adding ACL entry for group: "
                                         + groupName);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
        } catch (InvalidSessionKeyException e) {
            e.printStackTrace();
        } catch (CMException e) {
            e.printStackTrace();
        } catch (FinderException e) {
            e.printStackTrace();
        } finally {
            if (content != null) {
                try {
                    content.unlock();
                } catch (CMException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method used to create the groups used in the Greenfield application. If a
     * group already exists, nothing is done for that group.
     *
     * @throws RemoteException
     * @throws InvalidSessionKeyException
     * @throws PermissionDeniedException
     * @throws CreateException
     */
    private void createGroups()
        throws RemoteException,
               PermissionDeniedException,
               InvalidSessionKeyException,
               CreateException
    {
        for (int i = 0; i < GROUPS.length; i++) {
            System.err.print("Group " + GROUPS[i] + ": ");
            GroupId[] groupIds = _userServer.findGroupsByName(GROUPS[i]);

            if (groupIds != null && groupIds.length > 0) {
                System.err.println("already exists.");
            } else {
                System.err.print(" creating new..");
                Group newGroup = _userServer.createGroup(_caller.getUserId());

                newGroup.setName(GROUPS[i], _caller);
                System.err.println(" created. Id is: " + newGroup.getGroupId());
            }
        }
    }

    /**
     * Adds users to the created groups
     *
     */
    private void addGreenfieldTimesUsers()
    {
        try {
            addUserToGroup(GREENFIELD_TIMES_USERS[0], GROUP_GT_EDITORS);
            addUserToGroup(GREENFIELD_TIMES_USERS[1], GROUP_GT_EDITORS);
            addUserToGroup(GREENFIELD_TIMES_USERS[1], GROUP_GT_MODS);
            addUserToGroup(GREENFIELD_TIMES_USERS[2], GROUP_GT_ADMINS);
            addUserToGroup(GREENFIELD_TIMES_USERS[2], GROUP_GT_MODS);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (FinderException e) {
            e.printStackTrace();
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
        } catch (InvalidSessionKeyException e) {
            e.printStackTrace();
        }
    }

    private void addGreyhoundPostUsers()
    {
        try {
            addUserToGroup(GREYHOUND_POST_USERS[0], GROUP_GH_EDITORS);
            addUserToGroup(GREYHOUND_POST_USERS[1], GROUP_GH_EDITORS);
            addUserToGroup(GREYHOUND_POST_USERS[1], GROUP_GH_MODS);
            addUserToGroup(GREYHOUND_POST_USERS[2], GROUP_GH_ADMINS);
            addUserToGroup(GREYHOUND_POST_USERS[2], GROUP_GH_MODS);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (FinderException e) {
            e.printStackTrace();
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
        } catch (InvalidSessionKeyException e) {
            e.printStackTrace();
        }
    }

    private void addUserToGroup(String user, String groupName)
        throws RemoteException, FinderException, PermissionDeniedException, InvalidSessionKeyException
    {
        System.err.println("Adding user " + user + " to group " + groupName);
        Group group = _userServer.findGroup(_userServer.findGroupsByName(groupName)[0]);
        group.addMember(_userServer.getUserByLoginName(user).getUserId(), _caller);
    }

    // Create a web-user specific to GT
    private void setLoginNameForGreenfieldTimesUser()
    {
        try {
            ContentRead content = _cmServer.getContent(
                    new ExternalContentId(DEPARTMENT_GREENFIELD_TIMES));

            String gtId = content.getContentId().getContentId().getContentIdString();

            String loginName = "adrian@greenfieldtimes.com";
            String password = "adrian";
            String newLoginName = gtId + "_" + loginName;

            System.err.println("Changing login name of " + loginName + " to " + newLoginName
                               + " to make it a Greenfield Times site user.");

            User user = null;

            try {
                user = _userServer.getUserByLoginName(loginName);
            } catch (FinderException e) {
                System.err.println("Could not find user " + loginName + " skipping, maybe aready changed.");
                return;
            }

            user.setLoginName(newLoginName, _caller);
            user.setPassword(password, _caller);

            ContentPolicy ubg = (ContentPolicy) _cmServer.getPolicy(new ExternalContentId("ubg-0-adrian"));
            ContentId blogId = _cmServer.translateSymbolicContentId(new ExternalContentId("example.adrian.blog"));
            ubg = (ContentPolicy) _cmServer.createContentVersion(ubg.getContentId());
            ubg.setComponent("blogs/0", "value", blogId.getContentId().getContentIdString());
            ubg.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
