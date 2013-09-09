/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.atex.plugins.test.Driver;
import com.atex.plugins.test.timer.Wait;
import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.common.logging.LogUtil;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserServer;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class TwitterConfiguration {

    protected Selenium selenium;
    protected Driver robot;
    protected Wait wait;

    private static final String CONNECTION_PROPERTIES_URL = "http://localhost:8081/connection-properties/connection.properties";
    private final static Logger LOG = LogUtil.getLog(TwitterConfiguration.class);

    @Before
    public void before() throws Exception {
        String port = System.getProperty("jetty.port", "8080");
        int seleniumPort = Integer.valueOf(System.getProperty("selenium.port", "4123"));
        String browser = System.getProperty("browser", "*firefox");

        selenium = new DefaultSelenium("localhost", seleniumPort, browser, "http://localhost:" + port);
        selenium.start();
        robot = new Driver(selenium);
        wait = new Wait();
        // login to polopoly
        assertTrue("Failed to login", robot.login());
        robot.unlockAllContents();
    }

    @After
    public void after() throws Exception {
        selenium.stop();
        wait = null;
    }

    @AfterClass
    public static void resetDefaultValue() throws Exception {
        EjbCmClient cmClient = new EjbCmClient();

        Application application = new StandardApplication("twitter-plugin");
        application.addApplicationComponent(cmClient);
        application.readConnectionProperties(new ConnectionProperties(new URL(CONNECTION_PROPERTIES_URL)));
        application.init();

        UserServer userServer = cmClient.getUserServer();
        Caller caller = userServer.loginAndMerge("sysadmin", "sysadmin", null);

        PolicyCMServer policyCMServer = cmClient.getPolicyCMServer();
        policyCMServer.setCurrentCaller(caller);

        ExternalContentId extId = new ExternalContentId("com.atex.plugins.twitter.TwitterWidgetConfigHome");
        VersionedContentId vCId = extId.getLatestVersionId();

        Policy policy = policyCMServer.createContentVersion(vCId);
        ((SingleValuePolicy) policy.getChildPolicy("shellBgColor")).setValue("#8ec1da");
        ((SingleValuePolicy) policy.getChildPolicy("shellFgColor")).setValue("#ffffff");
        ((SingleValuePolicy) policy.getChildPolicy("tweetBgColor")).setValue("#ffffff");
        ((SingleValuePolicy) policy.getChildPolicy("tweetFgColor")).setValue("#444444");
        ((SingleValuePolicy) policy.getChildPolicy("tweetLinkColor")).setValue("#1985b5");
        ((SingleValuePolicy) policy.getChildPolicy("width")).setValue("auto");
        ((SingleValuePolicy) policy.getChildPolicy("height")).setValue("auto");
        ((SingleValuePolicy) policy.getChildPolicy("avatars")).setValue("true");
        ((SingleValuePolicy) policy.getChildPolicy("scrollbar")).setValue("true");
        ((SingleValuePolicy) policy.getChildPolicy("loop")).setValue("true");

        // fix content locked issue
        try {
            policyCMServer.commitContent(policy);
            LOG.log(Level.INFO, "Reset default value after test");
        } catch (Exception e) {
            policyCMServer.abortContent(policy, true);
            LOG.log(Level.WARNING, e.getMessage());
        }
    }

    @Test
    public void changeConfigThenVerifyOnFrontPage() {
        robot.search("com.atex.plugins.twitter.TwitterWidgetConfigHome").switchToWork();

        String shellBgcolor = "#FF0000";
        String shellBgColorLocator = "//fieldset[contains(@class,'shellBgColor')]//input";
        String shellFgcolor = "#FFFFFF";
        String shellFgColorLocator = "//fieldset[contains(@class,'shellFgColor')]//input";
        String tweetBgcolor = "#000000";
        String tweetBgColorLocator = "//fieldset[contains(@class,'tweetBgColor')]//input";
        String tweetFgcolor = "#FFFFFF";
        String tweetFgColorLocator = "//fieldset[contains(@class,'tweetFgColor')]//input";
        String tweetLinkcolor = "#FFEE00";
        String linkColorLocator = "//fieldset[contains(@class,'tweetLinkColor')]//input";

        String width = "222";
        String widthLocator = "//fieldset[contains(@class,'width')]//input";

        String height = "333";
        String heightLocator = "//fieldset[contains(@class,'height')]//input";

        String avatars = "false";
        String avatarsLocator = "//fieldset[contains(@class,'avatars')]//input";

        String scrollbar = "false";
        String scrollbarLocator = "//fieldset[contains(@class,'scrollbar')]//input";

        String loop = "false";
        String loopLocator = "//fieldset[contains(@class,'loop')]//input";

        robot.clickEdit();
        wait.forGuiChangeList();
        
        robot.type(shellBgColorLocator, shellBgcolor);
        robot.type(shellFgColorLocator, shellFgcolor);
        robot.type(tweetBgColorLocator, tweetBgcolor);
        robot.type(tweetFgColorLocator, tweetFgcolor);
        robot.type(linkColorLocator, tweetLinkcolor);
        robot.type(widthLocator, width);
        robot.type(heightLocator, height);
        robot.type(avatarsLocator, avatars);
        robot.type(scrollbarLocator, scrollbar);
        robot.type(loopLocator, loop);

        robot.clickSaveAndClose();
        wait.forGuiChangeList();
        
        robot.logout();
        // verify the changes of twitter element 
        selenium.open("/GreenfieldTimes.d");

        robot.waitForElement("css=.element.twitter");

        String htmlSource = selenium.getHtmlSource();

        String jsScript = "<script src=\"http://widgets.twimg.com/j/2/widget.js\"></script>\n"
                    +"<script type=\"text/javascript\">\n"
                    +"  new TWTR.Widget({\n"
                    +"    version: 2,\n"
                    +"    type: 'search',\n"
                    +"    interval: 5000,\n"
                    +"    search: '#Atex',\n"
                    +"    title: 'Atex',\n"
                    +"    subject: 'Atex News',\n"
                    +"    rpp: '10',\n"
                    +"    width: '215',\n"
                    +"    height: '333',\n"
                    +"    theme: {\n"
                    +"      shell: {\n"
                    +"        background: '#FF0000',\n"
                    +"        color: '#FFFFFF'\n"
                    +"      },\n"
                    +"    tweets: {\n"
                    +"      background: '#000000',\n"
                    +"      color: '#FFFFFF',\n"
                    +"      links: '#FFEE00'\n"
                    +"    }\n"
                    +"  },\n"
                    +"  features: {\n"
                    +"    scrollbar: false,\n"
                    +"    loop: false,\n"
                    +"    live: true,\n"
                    +"    avatars: false,\n"
                    +"    toptweets: true,\n"
                    +"        behavior: 'all'\n"
                    +"      }\n"
                    +"}).render().start();";

        assertTrue(htmlSource.contains(jsScript));
    }
}
