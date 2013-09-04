/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.atex.plugins.test.GuiDriver;

public class BrightcoveConfig {

    protected WebDriver webDriver;
    protected GuiDriver robot;
    
    String siteRootSiteXPath = "//fieldset[@class='field site']//ul[@class='tree']/li/a[text()='Sites']";
    String siteDepartmentXPath = "//fieldset[@class='field site']//ul[@class='subtree depth0']/li/a[text()='Greenfield Times']";
    String deptRootSiteXPath = "//fieldset[@class='field department']//ul[@class='tree']/li/a[text()='Sites']";
    String deptDepartmentXPath = "//fieldset[@class='field department']//ul[@class='subtree depth0']/li/a[text()='Greenfield Times']";

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

    @Test(timeout = 300000)
    public void verifyBrightcoveConfigInstalled() {
        String inputTemplateExtId = "com.atex.plugins.brightcove.BrightcoveConfigTemplate";
        String xpathExpName = "//input[contains(@value, 'com.atex.plugins.brightcove.BrightcoveConfigTemplate')]";
        robot.search(inputTemplateExtId).switchToWork();
        assertTrue("Cant find template name via text box", isElementPresent(xpathExpName, inputTemplateExtId));
    }

    @Test(timeout = 300000)
    public void testUpdateBrightcoveTokenConfig() {
        String nameVal = "Greenfield Site Config";
        String publishIdVal = "1715776696001";
        String readTokenVal = "9htdXeOZ1_eb2iuRW9ls72U_5vaRFzkjCxqdoPoxj_h9FZEb-aWMeQ..";
        String readTokenUrlVal = "9htdXeOZ1_fZaAHNKPUE7vJDpViOgAbOr4YuSFO_z_FO_n11JHyo8A..";
        String writeTokenVal = "9htdXeOZ1_fZaAHNKPUE7vJDpViOgAbO33aa2hux4PQeR-VrEq0XZQ..";
        String previewPlayerId = "1715350961001";
        String contentListEntryPath = String.format("//fieldset[@class='field brightcoveConfigs']//td[@class='content']/a[text()='%s']", nameVal);

        robot.search("com.atex.plugins.brightcove.BrightcoveConfigHome")
             .switchToWork();
        robot.click(By.xpath("//fieldset[@class='contentCreator field configCreator']/button"));

        // Select root sites for site field
        robot.click(siteRootSiteXPath);
        // Select department for site field
        robot.click(siteDepartmentXPath);

        // clear
        webDriver.findElement(By.xpath(".//fieldset[@class='field publisherId']/input")).clear();
        webDriver.findElement(By.xpath(".//fieldset[@class='field readToken']/input")).clear();
        webDriver.findElement(By.xpath(".//fieldset[@class='field readTokenUrl']/input")).clear();
        webDriver.findElement(By.xpath(".//fieldset[@class='field writeToken']/input")).clear();
        webDriver.findElement(By.xpath(".//fieldset[@class='field previewPlayerId']/input")).clear();

        robot.type(By.xpath("//fieldset[@class='field name']/input"), nameVal)
             .type(By.xpath("//fieldset[@class='field publisherId']/input"), publishIdVal)
             .type(By.xpath("//fieldset[@class='field readToken']/input"), readTokenVal)
             .type(By.xpath("//fieldset[@class='field readTokenUrl']/input"), readTokenUrlVal)
             .type(By.xpath("//fieldset[@class='field writeToken']/input"), writeTokenVal)
             .type(By.xpath("//fieldset[@class='field previewPlayerId']/input"), previewPlayerId);

        // Select root sites for department field
        robot.click(deptRootSiteXPath);
        // Select department for department field
        robot.click(deptDepartmentXPath);
        robot.clickInsert();
        robot.clickSaveAndView()
             .search("com.atex.plugins.brightcove.BrightcoveConfigHome")
             .switchToWork();
        robot.click(By.xpath(contentListEntryPath));
        WebElement _publisherId = webDriver.findElement(By
                .xpath(".//fieldset[@class='field publisherId']/span[@class='p_textOutput']"));
        WebElement _readToken = webDriver.findElement(By
                .xpath(".//fieldset[@class='field readToken']/span[@class='p_textOutput']"));
        WebElement _readTokenUrl = webDriver.findElement(By
                .xpath(".//fieldset[@class='field readTokenUrl']/span[@class='p_textOutput']"));
        WebElement _writeToken = webDriver.findElement(By
                .xpath(".//fieldset[@class='field writeToken']/span[@class='p_textOutput']"));
        WebElement _previewId = webDriver.findElement(By
                .xpath(".//fieldset[@class='field previewPlayerId']/span[@class='p_textOutput']"));

        assertEquals(publishIdVal, _publisherId.getText());
        assertEquals(readTokenVal, _readToken.getText());
        assertEquals(readTokenUrlVal, _readTokenUrl.getText());
        assertEquals(writeTokenVal, _writeToken.getText());
        assertEquals(previewPlayerId, _previewId.getText());
    }

    protected boolean isElementPresent(String xpathExpression, String value) {
        WebElement element = webDriver.findElement(By.xpath(xpathExpression));
        if (value.equals(element.getAttribute("value"))) {
            return true;
        }
        return false;
    }

    protected boolean isTextPresent(String xpathExpression, String value) {
        WebElement element = webDriver.findElement(By.xpath(xpathExpression));
        if (value.equals(element.getText())) {
            return true;
        }
        return false;
    }
}
