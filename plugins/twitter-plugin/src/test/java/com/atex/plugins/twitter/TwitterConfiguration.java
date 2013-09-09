/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.atex.plugins.test.GuiDriver;
import com.atex.plugins.test.timer.Wait;

public class TwitterConfiguration {

    protected WebDriver webDriver;
    protected GuiDriver robot;
    protected Wait wait;

    @Before
    public void before() throws Exception {
        String port = System.getProperty("jetty.port", "8080");

        webDriver = GuiDriver.getHeadlessWebDriver();
        wait = new Wait();

        robot = new GuiDriver(webDriver, "http://localhost:" + port);

        // login to polopoly
        assertTrue("Failed to login", robot.login());
    }

    @After
    public void after() throws Exception {
        webDriver.quit();
        wait = null;
    }

    @Test
    public void initTwitterAccountKeys() {
        robot.search("com.atex.plugins.twitter.TwitterConfigHome").switchToWork()
             .clickEdit()
             .openTab("Accounts")
             .click("//fieldset[@class='contentCreator field creator']/button")
             .type("css=.field.name input", "Jenkins Sport")
             .type("css=.field.consumerKey input", "qQTwINKzoKsOilMHprTCw")
             .type("css=.field.consumerSecret input", "yREqsK68D6QMAczWUO2pOrfXsvQ4Z5h6WDqJ2X4")
             .type("css=.field.accessToken input", "979876250-wisVHSk9S8ntFe5rrEEPVXSgm0kFBAxHwiuj8VBq")
             .type("css=.field.accessTokenSecret input", "OtxCTEDXbW8AEaHCBNrhp7uMZtE3u84HubkjiCo336o")
             .clickInsert()
             .clickSaveAndClose();
        robot.logout();
    }
}
