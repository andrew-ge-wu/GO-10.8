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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.atex.plugins.brightcove.service.BrightcoveService;
import com.atex.plugins.test.GuiDriver;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.polopoly.application.Application;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.ConnectionPropertiesConfigurationException;
import com.polopoly.application.ConnectionPropertiesParseException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.StandardApplication;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.util.FileUtil;

public class BrightcovePlugin {

    public static final String BUTTON_WITH_LABEL = "//button[contains(text(), '%s')]";
    public static final long TIMEOUT = TimeUnit.MINUTES.toMillis(3l);
    public static final long SLEEP = TimeUnit.SECONDS.toMillis(3l);
    private static final String CONNECTION_PROPERTIES_URL = "http://localhost:8081/connection-properties/connection.properties";
    
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

    @Test(timeout = 300000)
    public void verifyBrightcovePluginElementInstalled() {

        robot.search("com.atex.plugins.brightcove.MainElement").switchToWork();

        String xpathExpName = "//input[contains(@value, 'com.atex.plugins.brightcove.MainElement')]";
        String valueName = "com.atex.plugins.brightcove.MainElement";

        assertTrue("Cant find template name via text box", isElementPresent(xpathExpName, valueName));

        String xpathExpOt = "//a[contains(text(), 'com.atex.plugins.brightcove.MainElement.ot')]";
        String valueOt = "com.atex.plugins.brightcove.MainElement.ot";

        assertTrue("Cant find output template", isTextPresent(xpathExpOt, valueOt));
    }

    @Test(timeout = 300000)
    public void testCreateAndRenderBrightcoveElementVideo() {

        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Main column", "Brightcove element");

        robot.type("css=.field.name input", "Brightcove video")
             .type("css=.field.video input", "1715317598001")
             .type("css=.field.playerId input", "1715350963001")
             .clickInsert()
             .clickSaveAndClose()
             .logout();
 
        try {
        	webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            for (int i=0; i<10; i++) {
                robot.get("/GreenfieldTimes.d?param=" +i );
                if (robot.isElementPresent(By.xpath("//div[@id='col3']/div[@class='element brightcove']"))) {
                    break;
                }
            }
        } finally {
        	webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
    }

    @Test(timeout = 300000)
    public void testCreateAndRenderBrightcoveElementPlaylist() {

        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Right column", "Brightcove element");

        robot.type("css=.field.name input", "Brightcove playlist")
             .select("css=.group.playerType select", "Brightcove Playlist")
             .type("css=.field.playerId input", "1719577957001")
             .clickInsert()
             .clickSaveAndClose()
             .logout();
        try {
        	webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            for (int i=0; i<10; i++) {
                robot.get("/GreenfieldTimes.d?param=" +i );
                if (robot.isElementPresent(By.xpath("//div[@id='col4']/div[@class='element brightcove']"))) {
                    break;
                }
            }
        } finally {
        	webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        }
    }

    @Test
    public void searchByNameShouldReturnAtLeastOneResultAndPreviewShouldWorking() {
        String q = "bird";
        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Main column", "Brightcove element");
        robot
            .select("//fieldset[@class='field search']/select", "Name & Description")
            // type query
            .type("css=.field.search input[type=text]", q)
            // search
            .click(String.format(BUTTON_WITH_LABEL, "Search"))
            // select first result
            .click(String.format(BUTTON_WITH_LABEL, "Select"));
        // Sleep for one second for the field to populate
        long sleepPeriod = 1000L;
        try {
            Thread.sleep(sleepPeriod);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement selectButton = webDriver.findElement(By.xpath(String.format(BUTTON_WITH_LABEL, "Select")));
        WebElement brightcoveNameField = webDriver.findElement(By.cssSelector(".field.name input[type=text]"));
        WebElement brightcoveIdField = webDriver.findElement(By.cssSelector(".field.video input[type=text]"));
        String videoNameFromField = brightcoveNameField.getAttribute("value");
        String videoIdFromField = brightcoveIdField.getAttribute("value");
        String videoId = selectButton.getAttribute("data-id");
        String videoName = selectButton.getAttribute("data-name");
        assertEquals("Brightcove name is not same from search", videoName, videoNameFromField);
        assertEquals("Brightcove id is not same from search", videoId, videoIdFromField);
        assertTrue("Video name[" + videoName + "] does not contains keyword[" + q + "]", videoName.contains(q));
    }
    
    @Test
    public void searchByTagShouldReturnAtLeastOneResultAndPreviewShouldWorking() {
        String q = "sample";
        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Main column", "Brightcove element");
        robot
            // select Tags from drop down
            .select("//fieldset[@class='field search']/select", "Tags")
            // type query
            .type("css=.field.search input[type=text]", q)
            // search
            .click(String.format(BUTTON_WITH_LABEL, "Search"))
            // select first result
            .click(String.format(BUTTON_WITH_LABEL, "Select"));
        // Sleep for one second for the field to populate
        long sleepPeriod = 1000L;
        try {
            Thread.sleep(sleepPeriod);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement selectButton = webDriver.findElement(By.xpath(String.format(BUTTON_WITH_LABEL, "Select")));
        WebElement brightcoveIdField = webDriver.findElement(By.cssSelector(".field.video input[type=text]"));
        String videoIdFromField = brightcoveIdField.getAttribute("value");
        String videoId = selectButton.getAttribute("data-id");
        String videoTags = selectButton.getAttribute("data-tags");
        assertEquals("Brightcove id is not same from search", videoId, videoIdFromField);
        assertTrue("Video tags[" + videoTags + "] does not contains tag[" + q + "]", videoTags.contains(q));
    }
    
    @Test
    public void searchByReferenceIdShouldReturnAtLeastOneResultAndPreviewShouldWorking() {
        String q = "bird.id";
        robot.search("GreenfieldTimes.d").switchToWork();
        robot.createElementInStartPageColumn("Main column", "Brightcove element");
        robot
            // select Reference ID from drop down
            .select("//fieldset[@class='field search']/select", "Reference ID")
            // type query
            .type("css=.field.search input[type=text]", q)
            // search
            .click(String.format(BUTTON_WITH_LABEL, "Search"))
            // select first result
            .click(String.format(BUTTON_WITH_LABEL, "Select"));
        // Sleep for one second for the field to populate
        long sleepPeriod = 1000L;
        try {
            Thread.sleep(sleepPeriod);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement selectButton = webDriver.findElement(By.xpath(String.format(BUTTON_WITH_LABEL, "Select")));
        WebElement brightcoveIdField = webDriver.findElement(By.cssSelector(".field.video input[type=text]"));
        String videoIdFromField = brightcoveIdField.getAttribute("value");
        String videoId = selectButton.getAttribute("data-id");
        String videoRerefenceId = selectButton.getAttribute("data-reference-id");
        assertEquals("Brightcove id is not same from search", videoId, videoIdFromField);
        assertTrue("Video reference id[" + videoRerefenceId + "] does not match with reference id[" + q + "]", videoRerefenceId.contains(q));
    }
    
    @Ignore
    @Test
    public void uploadFileToBrightcoveAndVerifyNameDescritionAndTags() throws InterruptedException, CMException, BrightcoveException, IllegalArgumentException, ConnectionPropertiesConfigurationException, ConnectionPropertiesParseException, MalformedURLException, IllegalApplicationStateException, IOException {
        int r = nextInt();
        String name = "brightcove name " + r;
        String description = "brightoce description " + r;
        String tag = "Obama";
        
        String videoPath = getFilePath("small.flv");
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openArticlesAndResourcesTab()
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "Brightcove Video")
            .click(String.format(BUTTON_WITH_LABEL, "Create"))
            .type("css=.field.name input", name)
            .type("css=.field.lead textarea", description)
            // categorization
            .click("//li/a[contains(text(), 'labour')]")
            // free text categorization
            .click("//span[contains(text(), 'Person')]")
            .type("css=textarea.tagCategoryInput", tag);
        webDriver.findElement(By.cssSelector(".field.upload input[type='file']")).sendKeys(videoPath);
        
        robot
            .click(String.format(BUTTON_WITH_LABEL, "Push to Brightcove"));
        Thread.sleep(SLEEP);
        WebElement videoIdField = webDriver.findElement(By.cssSelector(".field.video input"));
        String videoId = videoIdField.getAttribute("value");
        BrightcoveService service = new BrightcoveService(getPolicyCMServer("brighcove-plugin"));
        Video video = null;
        long start = System.currentTimeMillis();
        long now = start;
        // timeout in 3 minutes
        while((now - start) <= TIMEOUT) {
            try {
                video = service.findByVideoID(Long.parseLong(videoId));
                assertNotNull("Failed to find video[" + videoId + "]", video);
                assertEquals("Video name is not same", name, video.getName());
                assertEquals("Video description is not same", description, video.getShortDescription());
                assertTrue("Video tags[" + video.getTags() + "] does not contain " + tag, video.getTags().contains(tag));
                assertTrue("Video tags[" + video.getTags() + "] does not contain labour", video.getTags().contains("labour"));
                // delete test video
                service.deleteVideo(video.getId());
                return;
            } catch (BrightcoveException e) {
                System.out.println(e.getMessage());
            }
            Thread.sleep(SLEEP);
        }
        throw new CMException("Failed to find video[" + videoId + "] in 3 minutes");
    }
    
    @Test
    public void pullVideoFromBrightcove() throws InterruptedException {
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openArticlesAndResourcesTab()
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "Brightcove Video")
            .click(String.format(BUTTON_WITH_LABEL, "Create"))
            .type("css=.field.video input", "1715317598001")
            .click(String.format(BUTTON_WITH_LABEL, "Pull info from Brightcove"));
        WebElement nameElement = webDriver.findElement(By.cssSelector(".field.name input"));
        // a hack to select element again after resubmit form
        nameElement = webDriver.findElement(By.cssSelector(".field.name input"));
        WebElement shortDescriptionElement = webDriver.findElement(By.cssSelector(".field.lead textarea"));
        assertTrue("Video name is not populated", !nameElement.getAttribute("value").trim().isEmpty());
        // use getAttribute() instead of getText()
        // http://code.google.com/p/selenium/issues/detail?id=2443
        assertTrue("Video short description is not populated", !shortDescriptionElement.getAttribute("value").trim().isEmpty());
        robot.clickSaveAndInsert();
    }

    @Test
    public void createTvElement() {
        String name = "Amazing Animals" + nextInt();
        
        robot.search("GreenfieldTimes.d");
        robot.switchToWork();
        robot.openArticlesAndResourcesTab();
        // copy previously created brightcove resource
        robot.click("css=.toc .p_listentry .tools button");
        robot.openSourcesTab();
        // create PQ
        robot.select(GuiDriver.CONTENT_CREATOR_SELECT, "Manual Publishing Queue");
        robot.clickCreate();
        robot.type("css=.field.name input", "Brightcove TV element");
        // paste brightcove resource
        robot.click("css=.field.default button[title=Paste]");
        robot.clickSaveAndInsert();
        // copy PQ
        robot.click("css=.toc .p_listentry .tools button");
        robot.openStartPageTab();
        // create Brightcove Slider
        robot.select(GuiDriver.CONTENT_CREATOR_SELECT, "Brightcove List element");
        robot.clickCreate();
        robot.type("css=.field.name input", "Brightcove Slider");
        robot.select("css=.group.mode select", "Slide");
        robot.click("css=.field.videos button[title=Paste]");
        // select greenfield times from department tree
        robot.select("//fieldset[@class='group display']//select", "A specific department");
        robot.click("//fieldset[@class='field selectedDept']//ul[@class='subtree depth0']/li/a[contains(text(), 'Greenfield Times')]");
        robot.clickInsert();
        robot.clickSaveAndView();
        // copy slider
        robot.click("css=.toc .p_listentry .tools button");
        // create Brightcove TV element
        robot.select(GuiDriver.CONTENT_CREATOR_SELECT, "Brightcove TV element");
        robot.clickCreate();
        robot.type("css=.field.name input", name);
        robot.type("css=.field.player input", "1715350961001");
        robot.click("css=.field.showSlider input");
        // paste in PQ
        robot.click("css=.field.videos button[title=Paste]");
        robot.clickInsert();
        robot.clickSaveAndView();
        robot.logout();
        robot.get("/GreenfieldTimes.d");
        WebElement header = webDriver.findElement(By.xpath("//div[@class='element tv']/h1"));
        // verify
        assertFalse("Header text is empty", header.getText().isEmpty());
    }

    @Test(timeout = 300000)
    public void testCreateAndRenderElementLocalPlaylist() {
        String name = "Local video" + nextInt();
        robot
            .search("GreenfieldTimes.d")
            .switchToWork()
            .openArticlesAndResourcesTab()
            // copy previously created brightcove resource to new pq
            .click("css=.toc .p_listentry .tools button")
            .openSourcesTab()
            // create PQ
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "Manual Publishing Queue")
            .clickCreate()
            .type("css=.field.name input", "Brightcove Element PQ")
            // paste brightcove resource
            .click("css=.field.default button[title=Paste]")
            .clickSaveAndInsert()
            // copy PQ
            .click("css=.toc .p_listentry .tools button")
            .openStartPageTab()
            // create Brightcove element
            .select(GuiDriver.CONTENT_CREATOR_SELECT, "Brightcove element")
            .clickCreate()
            .type("css=.field.name input", name)
             .select("css=.group.playerType select", "Playlist")
             .type("css=.field.playerId input", "1715350963001")
            // paste in PQ
            .click("css=.field.videoContainer button[title=Paste]")
            .clickInsert()
            .clickSaveAndView()
            .logout();
        robot.get("/GreenfieldTimes.d");
        robot.waitForElement("//div[@class='element brightcove']");
    }

    int nextInt() {
        return random.nextInt(10);
    }
    
    /**
     * Copy file in jar to tmp dir
     * 
     * @param file
     *            path in jar
     * @return file path in file system
     * @throws FileNotFoundException
     *             when file not found
     */
    private String getFilePath(String file) throws FileNotFoundException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpFile = new File(tmpDir, file);
        InputStream in = null;
        OutputStream os = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(file);
            os = new FileOutputStream(tmpFile);
            FileUtil.copyFile(in, os);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(os);
        }
        return tmpFile.getPath();
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
    
    private PolicyCMServer getPolicyCMServer(String name) throws IllegalArgumentException, IllegalApplicationStateException, ConnectionPropertiesConfigurationException, ConnectionPropertiesParseException, MalformedURLException, IOException {
        Application application = new StandardApplication(name);
        EjbCmClient cmClient = new EjbCmClient();
        application.addApplicationComponent(cmClient);
        application.readConnectionProperties(new ConnectionProperties(new URL(CONNECTION_PROPERTIES_URL)));
        application.init();
        return cmClient.getPolicyCMServer();
    }
}
