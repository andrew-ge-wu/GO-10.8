/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import com.atex.plugins.test.GuiDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.Random;

import static org.junit.Assert.*;

public class ListElement {

    protected WebDriver webDriver;
    protected GuiDriver robot;
    static final Random random = new Random();

    @Before
    public void setUp() throws Exception {
        String port = System.getProperty("jetty.port", "8080");
        webDriver = GuiDriver.getHeadlessWebDriver();

        robot = new GuiDriver(webDriver, "http://localhost:" + port);
        assertTrue("Failed to login", robot.login());
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
        webDriver = null;
        robot = null;
    }

    @Test
    public void createListElementAndVerifyTheNameAtFrontEnd() throws InterruptedException {
        // random name for us to do verification
        String name = "brightcove list element " + random.nextInt();
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openSourcesTab()
            .select("//select", "Metadata Driven Publishing Queue")
            .clickCreate()
            .type("css=.field.name input", "Name")
            .click("css=.subtree.depth0 li:nth-child(2) a")
            .click("css=.subtree.depth1 li:nth-child(2) a")
            .click(By.xpath("//a[contains(text(), 'com.atex.plugins.brightcove.video.MainElement')]"))
            .clickSaveAndInsert()
            .click("css=.toc button.clipboard.icon.enabledAfterMove")
            .openStartPageTab()
            .select("css=.layoutContentCreatorInHeader select", "Brightcove List element")
            .clickCreate()
            .type("css=.field.name input", name)
            .click(By.xpath("//button[@title = 'Paste']"))
            .click("//fieldset[@class='field displayTitle']/input")
            .select("//fieldset[@class='group display']//select", "A specific department")
            .click("//fieldset[@class='field selectedDept']//ul[@class='subtree depth0']/li/a[contains(text(), 'Greenfield Times')]")
            .clickInsert()
            .clickSaveAndView()
            .get("/GreenfieldTimes.d")
            .waitForElement("//a[contains(text(), '" + name + "')]");
    }

    @Test
    public void createSlideElementAndVerifyTheNameAtFrontEnd() throws InterruptedException {
        // random name for us to do verification
        String name = "brightcove list element " + random.nextInt();
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openSourcesTab()
            .select("//select", "Metadata Driven Publishing Queue")
            .clickCreate()
            .type("css=.field.name input", "SlideViewPQ")
            .click("css=.subtree.depth0 li:nth-child(2) a")
            .click("css=.subtree.depth1 li:nth-child(2) a")
            .click(By.xpath("//a[contains(text(), 'com.atex.plugins.brightcove.video.MainElement')]"))
            .clickSaveAndInsert()
            .click("css=.toc button.clipboard.icon.enabledAfterMove")
            .openStartPageTab()
            .select("css=.layoutContentCreatorInHeader select", "Brightcove List element")
            .clickCreate()
            .type("css=.field.name input", name)
            .click(By.xpath("//button[@title = 'Paste']"))
            .select("//select", "Slide")
            .select("//fieldset[@class='group display']//select", "A specific department")
            .click("css=.field.selectedDept .subtree.depth0 a.defaultAction")
            .clickInsert()
            .clickSaveAndView()
            .get("/GreenfieldTimes.d")
            .waitForElement("//div[@class='element brightcove']/div[@class='gallery']");
    }
}
