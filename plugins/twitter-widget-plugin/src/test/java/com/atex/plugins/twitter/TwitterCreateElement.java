/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atex.plugins.test.Driver;
import com.atex.plugins.test.timer.Wait;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class TwitterCreateElement {

    protected Selenium selenium;
    protected Driver robot;
    protected Wait wait;

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
    }

    @After
    public void after() throws Exception {
        selenium.stop();
        wait = null;
    }

    @Test
    public void twitterPluginIsInstalled() {
        // search twitter plugin
        robot.search("com.atex.plugins.twitter.MainElement").switchToWork();
        assertTrue("Cant find template name via text box",
                selenium.isElementPresent("//input[@value='com.atex.plugins.twitter.MainElement']"));
        wait.forGuiChangeList();
        assertTrue("Cant find output template", selenium.isTextPresent("com.atex.plugins.twitter.MainElement.ot"));
    }

    @Test
    public void createTwitterHashtagElementThenVerifyOnFrontPage() {
        robot.search("GreenfieldTimes.d").switchToWork();
        selenium.select("css=.layoutContentCreator select", "Twitter hashtags element");
        selenium.click("css=.layoutContentCreator button");

        String name = "css=.field.name input";
        robot.waitForElement(name);
        robot.keyin(name, "Atex");
        String title = "css=.field.title input";
        robot.keyin(title, "Atex News");
        String searchQuery = "css=.field.hashtags input";
        robot.keyin(searchQuery, "#Atex");
        String numberOfTwitts = "css=.field.rpp input";
        robot.keyin(numberOfTwitts, "10");

        // insert, save, close
        robot.clickInsert();
        robot.clickSaveAndClose();
        wait.forGuiChangeList();
        robot.logout();

        // verify the twitter element is in place
        selenium.open("/GreenfieldTimes.d");
        robot.waitForElement("//div[@id='col2']/div[@class='element twitter']");
    }
}
