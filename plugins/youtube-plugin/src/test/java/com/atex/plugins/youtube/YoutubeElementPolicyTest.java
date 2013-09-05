package com.atex.plugins.youtube;

import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.Content;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;

public class YoutubeElementPolicyTest {
    private YoutubeElementPolicy target;

    private HashMap<String, Policy> children = new HashMap<String, Policy>();

    @Mock
    Content content;
    @Mock
    InputTemplate inputTemplate;
    @Mock
    Policy parent;
    @Mock
    PolicyCMServer cmServer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        children.put(YoutubeElementPolicy.YID, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.TITLE, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.HEIGHT, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.WIDTH, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.LINK, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.DESCRIPTION, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.START_TIME, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.THUMBNAIL_URL, new SingleValuePolicyMock());
        children.put(YoutubeElementPolicy.PLAY_TIME, new SingleValuePolicyMock());

        target = new YoutubeElementPolicy() {
            @Override
            protected void initChildPolicies() {
                this.childPolicies = children;
            }

            @Override
            public Policy getChildPolicy(String policyName) {
                if (this.childPolicies == null) {
                    initChildPolicies();
                }
                return (Policy) childPolicies.get(policyName);
            }

            @Override
            protected void initSelf() {
                
            }
        };

        target.init("PolicyName", new Content[] { content }, inputTemplate, parent, cmServer);
    }

    @Test
    public void testYid() {
        String expected = "This is yid";
        target.setYid(expected);

        String actual = target.getYid();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testTitle() {
        String expected = "This is title";
        target.setTitle(expected);

        String actual = target.getTitle();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testHeight() {
        target.setHeight("100");
        Assert.assertEquals(100, target.getHeight());
    }

    @Test
    public void testHeight2() {
        target.setHeight("aaa");
        Assert.assertEquals(0, target.getHeight());
    }

    @Test
    public void testWidth() {
        target.setWidth("110");
        Assert.assertEquals(110, target.getWidth());
    }

    @Test
    public void testWidth2() {
        target.setHeight("bbb");
        Assert.assertEquals(0, target.getWidth());
    }

    @Test
    public void testLink() {
        String expected = "xyzff";
        target.setLink(expected);

        String actual = target.getLink();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testDescription() {
        String expected = "xyzff";
        target.setDescription(expected);

        String actual = target.getDescription();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testStartTime() {
        target.setStartTime(100);
        Assert.assertEquals(100, target.getStartTime());
    }

    @Test
    public void testStartTime2() {
        target.setStartTime(-100);
        Assert.assertEquals(-1, target.getStartTime());
    }

    @Test
    public void testStartTime3() throws CMException {
        SingleValuePolicy policy = (SingleValuePolicy) children.get(YoutubeElementPolicy.START_TIME);
        policy.setValue("invalid number");
        Assert.assertEquals(-1, target.getStartTime());
    }

    @Test
    public void testPlayTime() {
        target.setPlayTime(100);
        Assert.assertEquals(100, target.getPlayTime());
    }

    @Test
    public void testPlayTime2() {
        target.setPlayTime(-100);
        Assert.assertEquals(-1, target.getPlayTime());
    }

    @Test
    public void testPlayTime3() throws CMException {
        SingleValuePolicy policy = (SingleValuePolicy) children.get(YoutubeElementPolicy.PLAY_TIME);
        policy.setValue("invalid number");
        Assert.assertEquals(-1, target.getPlayTime());
    }

    @Test
    public void testThumbnailUrl() {
        String expected = "thumbnailUrl.com/xxx.jpg";
        target.setThumbnailUrl(expected);

        String actual = target.getThumbnailUrl();
        Assert.assertEquals(expected, actual);
    }

    private class SingleValuePolicyMock extends SingleValuePolicy {
        private String value;

        @Override
        public void setValue(String value) throws CMException {
            this.value = value;
        }

        @Override
        public String getValue() throws CMException {
            return value;
        }
    }
}
