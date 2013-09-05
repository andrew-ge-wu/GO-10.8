/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.youtube;

import static junit.framework.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.atex.plugins.test.GuiDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class YoutubePluginIT {

    protected WebDriver webDriver;
    protected GuiDriver robot;

    @Test(timeout = 300000)
    public void youtubePluginIsInstalled() {
        // search youtube plugin
        robot.search("com.atex.plugins.youtube.MainElement").switchToWork();

        String xpathExpName = "//input[contains(@value, 'com.atex.plugins.youtube.MainElement')]";
        String valueName = "com.atex.plugins.youtube.MainElement";

        assertTrue("Cant find template name via text box", isElementPresent(xpathExpName, valueName));

        String xpathExpOt = "//a[contains(text(), 'com.atex.plugins.youtube.MainElement.ot')]";
        String valueOt = "com.atex.plugins.youtube.MainElement.ot";

        assertTrue("Cant find output template", isTextPresent(xpathExpOt, valueOt));
    }

    @Test(timeout = 300000)
    public void createYoutubeElement() throws InterruptedException {
        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Main column", "Youtube Player Element");

        // search the content from youtube
        String searchKey = "Samsung Galaxy Nexus 720p Display Review";
        String search = "css=.field.search input";
        robot.waitForElement(search);

        robot.keyin(search, searchKey);
        String searchButton = "css=.field.search button";
        robot.click(searchButton);

        // select the result
        String selectButton = "//button[@title='" + searchKey + "']";
        robot.click(selectButton);

        // we must use thread sleep, because the robot.clickInsert is actually able to execute, 
        // even the frame still loading, 
        // because the insert button is there before and after the click, 
        // this is to solve the issue in jenkins whereby the frame is load slowly.
        Thread.sleep(1000);
        // insert, save, close
        robot.clickInsert();
        robot.clickSaveAndClose();
        robot.logout();

        // verify the youtube element is in place
        robot.get("/GreenfieldTimes.d")
            .waitForElementRenderAtSite("//div[@id='col3']/div[@class='element youtube']");

    }

    @Test
    @Ignore
    public void searchVideoShouldReturnResults() {
        // search System Department
        // robot.search("p.SystemDepartment").switchToWork();
        // assertTrue("Cant find Youtube tab under System Department",
        // selenium.isElementPresent("//a[contains(text(), 'Youtube')]"));
        // // open youtube tab
        // selenium.click("//a[contains(text(), 'Youtube')]");
        //
        // String searchBox = String.format(Driver.INPUT_BY_NAME, "Keywords");
        // robot.waitForElement(searchBox, 2000);
        // // input keyword "adele"
        // robot.keyin(searchBox, "adele");
        // // do search
        // selenium.click("//button[@value='Search']");
        // // wait for "Rolling In The Deep" to appear, limit to 2 seconds
        // robot.waitForText("Rolling In The Deep", 20000);
    }

    @Before
    public void before() {
        String port = System.getProperty("jetty.port", "8080");
        webDriver = GuiDriver.getHeadlessWebDriver();

        robot = new GuiDriver(webDriver, "http://localhost:" + port);
        // login to polopoly
        assertTrue("Failed to login", robot.login());
    }

    @After
    public void after() {
        webDriver.quit();
    }

    private boolean isElementPresent(String xpathExpression, String value) {
        WebElement element = webDriver.findElement(By.xpath(xpathExpression));
        if (value.equals(element.getAttribute("value"))) {
            return true;
        }
        return false;
    }

    private boolean isTextPresent(String xpathExpression, String value) {
        WebElement element = webDriver.findElement(By.xpath(xpathExpression));
        if (value.equals(element.getText())) {
            return true;
        }
        return false;
    }
}
