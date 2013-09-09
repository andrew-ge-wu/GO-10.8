/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.atex.plugins.test.GuiDriver;
import com.atex.plugins.test.timer.Wait;

public class TwittableContent {

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

    @Test(timeout = 300000)
    public void tweetAnArticle() {
        String name = "Name ";
        String lead = "lead";
        String tweetText = "Tweet text";
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String now = dtFormat.format(new Date());
        robot.search("com.atex.plugins.twitter.TwitterConfigHome")
             .switchToWork()
             .clickEdit()
             .openTab("Accounts")
             .click("css=.field.accounts button[title='Copy to clipboard']")
             .clickSaveAndClose();
        robot.search("GreenfieldTimes.d")
             .switchToWork()
             .openArticlesAndResourcesTab()
             .selectStandardArticle()
             .clickCreate()
             .type("css=.field.name input", name + " " + now)
             .type("css=.field.lead textarea", lead)
             .click("css=.field.tweetitnow input")
             .type("css=.field.tweetText input", tweetText + " " + now)
             .click("css=.field.account button[title='Paste content reference']")
             .clickSaveAndInsert()
             .logout();

        robot = new GuiDriver(webDriver, "http://twitter.com");
        try {
            webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            for (int i=0; i<10; i++) {
                robot.get("/ppjenkinssport");
                if (robot.isElementPresent(By.xpath("//div[@class='content']/p[contains(text(), '" + "Tweet text "+ now + "')]"))) {
                    break;
                }
            }
        } finally {
            webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
    }

}
