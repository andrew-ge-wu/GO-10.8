/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.deptdrpq;

import static junit.framework.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.atex.plugins.test.GuiDriver;

public class DeptPQUseCaseIT {
    protected GuiDriver robot;
    protected WebDriver webDriver;

    @Before
    public void before() {
        String port = System.getProperty("jetty.port", "8080");

        webDriver = GuiDriver.getHeadlessWebDriver();

        robot = new GuiDriver(webDriver, "http://localhost:" + port);

        // login to polopoly
        assertTrue("Failed to login", robot.login());
        robot.unlockAllContents();
    }

    @After
    public void after() {
        webDriver.quit();
    }

    /**
     * Time out in 5 minutes
     */
    @Test(timeout = 300000)
    public void createAutoDepartmentPublishingQueueAndAttachToListElement() {
        robot.search("GreenfieldTimes.d").switchToWork();

        robot
        // go to sources tab
        .openSourcesTab() //
                // select Dept Driven PQ
                .select("css=.sourcesContentCreator select", "Department Driven Publishing Queue") //
                // click create button
                .click("css=.sourcesContentCreator button") //
                // type PQ name
                .type("css=.field.name input", "Dept PQ") //

                // select article as content type
                .click("//li/a[contains(text(), 'Article')]") //
                // insert
                .clickSaveAndInsert() //
                // copy new PQ into clipboard
                .click("css=.tools button") //
                // open start page
                .openStartPageTab() //
                // create List element at Right Top column
                .createElementInStartPageColumn("Right Top column", "List element") //
                // type name
                .type("css=.field.name input", "Dept PQ List") //
                // max size
                .type("css=.field.maximumAmount input", "10") //
                // paste previous created PQ
                .click("css=.publishingQueue button[title='Paste']") //
                // save
                .clickInsert();
        robot.clickSaveAndView();
        // wait for save, fixed for jenkins

        // verify PQ have article
        robot.logout();
        robot.get("/GreenfieldTimes.d");
        robot.waitForElement("//div[@id='col4']/div[@class='element list']");
    }

    /**
     * Time out in 5 minutes
     */
    @Test(timeout = 300000)
    public void createManualDepartmentPublishingQueueAndAttachToListElement() {
        robot.search("GreenfieldTimes.d").switchToWork();

        robot
        // go to sources tab.
        .openSourcesTab() //
                // select Dept Driven PQ
                .select("css=.sourcesContentCreator select", "Department Driven Publishing Queue") //
                // click create button
                .click("css=.sourcesContentCreator button") //
                // type PQ name
                .type("css=.field.name input", "Non-Auto Dept PQ") //
                // uncheck current department checkbox
                .click("//fieldset[contains(@class,'autoDepartment')]//input[@type='radio' and @value='false']") //
                // select GT
                .click("//li/a[contains(text(), 'Greenfield Times')]") //

                // select article as content type
                .click("//li/a[contains(text(), 'Article')]") //
                // insert
                .clickSaveAndInsert() //
                // copy new PQ into clipboard
                .click("css=.tools button") //
                // open start page
                .openStartPageTab() //
                // create List element at Right Top column
                .createElementInStartPageColumn("Right column", "List element") //

                // type name
                .type("css=.field.name input", "Non-Auto Dept PQ List") //
                // max size
                .type("css=.field.maximumAmount input", "10") //
                // paste previous created PQ
                .click("css=.publishingQueue button[title='Paste']") //
                // save
                .clickInsert();
        robot.clickSaveAndView();

        // verify PQ have article
        robot.logout();
        robot.get("/GreenfieldTimes.d");
        robot.waitForElement("//div[@id='col4']/div[@class='element list']");
    }
}
