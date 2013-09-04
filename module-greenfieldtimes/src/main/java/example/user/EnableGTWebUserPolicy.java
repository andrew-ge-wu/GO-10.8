package example.user;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.user.server.User;
import com.polopoly.user.server.UserId;
import com.polopoly.user.server.UserServer;

public class EnableGTWebUserPolicy
    extends ContentPolicy
{
    private final UserServer userServer;
    private final PolicyCMServer cmServer;

    private static final ExternalContentId GREENFIELDTIMES_EXTERNAL_ID = new ExternalContentId("GreenfieldTimes.d");

    public EnableGTWebUserPolicy(final CmClient client)
    {
        this.cmServer = client.getPolicyCMServer();
        this.userServer = client.getUserServer();
    }

    public void setEnableWebUser(final String userIdAndPasswordString)
        throws Exception
    {
        String greenfieldTimesId =
                cmServer.translateSymbolicContentId(GREENFIELDTIMES_EXTERNAL_ID).getContentId().getContentIdString();

        String[] split = userIdAndPasswordString.split(":");

        if (split.length != 2) {
            throw new RuntimeException(userIdAndPasswordString + " is invalid, required userId:password");
        }

        User user = userServer.getUserByUserId(new UserId(split[0]));
        String loginName = user.getLoginName();

        if (!loginName.startsWith(greenfieldTimesId)) {
            String newLoginName = greenfieldTimesId + "_" + loginName;
            user.setLoginName(newLoginName, cmServer.getCurrentCaller());
        }

        user.setPassword(split[1], cmServer.getCurrentCaller());
    }

    public void setEnableWebBlog(final String serviceAndBlogIdString)
        throws Exception
    {
        String[] split = serviceAndBlogIdString.split(":");

        if (split.length != 2) {
            throw new RuntimeException(serviceAndBlogIdString + " is invalid, required serviceExternalId:blogExternalId");
        }

        ContentPolicy ubg = (ContentPolicy) cmServer.getPolicy(new ExternalContentId(split[0]));
        ContentId blogId = cmServer.translateSymbolicContentId(new ExternalContentId(split[1]));

        ubg = (ContentPolicy) cmServer.createContentVersion(ubg.getContentId());
        ubg.setComponent("blogs/0", "value", blogId.getContentId().getContentIdString());

        ubg.commit();
    }
}
