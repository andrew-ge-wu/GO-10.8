package com.atex.plugins.brightcove;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.atex.plugins.test.GuiDriver;

public class BrightcoveVideoResource {

    protected WebDriver webDriver;
    protected GuiDriver robot;

    Random random = new Random();

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
    }

    private void createListElement(String maxAmt, String viewMode) {
        String eleName = "List Element " + nextInt();
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openArticlesAndResourcesTab()
            // copy previously created brightcove resource
            .click("css=.toc .p_listentry .tools button")
            .openSourcesTab()
            // create PQ
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "Manual Publishing Queue")
            .clickCreate()
            .type("css=.field.name input", eleName)
            // paste brightcove resource
            .click("css=.field.default button[title=Paste]")
            .clickSaveAndInsert()
            // copy PQ
            .click("css=.toc .p_listentry .tools button")
            .openStartPageTab()
            // create List element
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "List element")
            .clickCreate()
            .type("css=.field.name input", eleName)
            .type("css=.field.maximumAmount input", maxAmt)
            .click(By.xpath("//fieldset[@class='field viewMode']/div/div/input[@value='"+viewMode+"']"))
            // paste in PQ
            .click("css=.field.publishingQueue button[title=Paste]")
            .clickInsert()
            .clickSaveAndView()
            .logout();
    }

    /*
     * This test case is depend on
     * BrightcovePlugin.pullVideoFromBrightcove()
     * If this test case fail, make sure above test case is pass
     */
    @Test
    public void shouldRenderInListAsTeaser() {
        createListElement("5", "teaser");
        robot.get("/GreenfieldTimes.d");
        WebElement header = webDriver.findElement(By.xpath("//div[@class='brightcove teaserCol content']"));
        assertEquals("Header text is not correct", "amazon (do not delete this video)", header.getText());
    }

    @Test
    public void shouldRenderInListAsLinks() {
        createListElement("5", "link");
        robot.get("/GreenfieldTimes.d");
        WebElement header = webDriver.findElement(By.xpath("//div[@class='element list']/ul/li/a"));
        // verify
        System.out.println("head text links " + header.getText());
        assertEquals("Header text is not correct", "amazon (do not delete this video)", header.getText());
    }

    @Test
    public void shouldRenderAsContent() {
        robot.get("/GreenfieldTimes.d");
        WebElement listItem = webDriver.findElement(By.xpath("//div[@class='element list']/ul/li/a"));
        listItem.click();
        WebElement header = webDriver.findElement(By.xpath("//div[@class='element article']/div/h1"));
        // verify
        System.out.println("head text links " + header.getText());
        assertEquals("Header text is not correct", "amazon (do not delete this video)", header.getText());
    }

    int nextInt() {
        return random.nextInt(10);
    }
}
