package com.atex.plugins.youtube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;

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

public class YoutubeFieldPolicyTest {

    @Mock
    private YoutubeFieldPolicy field;

    @Mock
    private HashMap<String, Policy> children = new HashMap<String, Policy>();

    @Mock
    private Content content;

    @Mock
    private InputTemplate inputTemplate;

    @Mock
    private Policy parent;

    @Mock
    private PolicyCMServer cmServer;

    @Mock
    private SingleValuePolicy idPolicy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        field = new YoutubeFieldPolicy() {
            @Override
            protected void initChildPolicies() {
                this.childPolicies = children;
            }
            @Override
            protected void initSelf() {
            }
        };

        field.init("policy", new Content[] { content }, inputTemplate, parent, cmServer);

        when(children.get("id")).thenReturn(idPolicy);
    }

    @Test
    public void testGetIdIsEmpty() {
        assertEquals("", field.getId());
    }

    @Test
    public void testGetId() throws CMException {
        when(field.getChildValue("id")).thenReturn("testId");
        assertNotNull(field.getId());
        assertEquals("testId", field.getId());
    }

}
