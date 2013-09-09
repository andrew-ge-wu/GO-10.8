/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.twitter.widget;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.atex.plugins.twitter.TwittableInputPolicy;
import com.atex.plugins.twitter.TwitterAccount;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.orchid.widget.OContentSingleSelect;
import com.polopoly.cm.app.policy.SingleReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;

public class OTwitterAccountSelectWidgetTest {

    OTwitterAccountSelectWidget target;

    @Mock
    PrepareResult prepareResult;
    @Mock
    PolicyWidget policyWidget;
    @Mock
    TwittableInputPolicy parentPolicy;
    @Mock
    OrchidContext oc;
    @Mock
    MockSingleReference singleReference;
    @Mock
    PolicyCMServer cmServer;
    @Mock
    ContentSession contentSession;
    Caller caller;
    @Mock
    UserId userId;
    @Mock
    Policy policy;
    @Mock
    OContentSingleSelect contentSelect;
    static final String USER_PRINCIPAL = "sysadmin";

    @Before
    public void before() throws CMException {
        MockitoAnnotations.initMocks(this);
        caller = new Caller(userId);
        target = spy(new OTwitterAccountSelectWidget());
        doReturn(prepareResult).when(target).getResult();
        doReturn(policyWidget).when(target).getParentPolicyWidget();
        doReturn(oc).when(target).getOrchidContext();
        doReturn(singleReference).when(target).getPolicy();
        doReturn(contentSession).when(target).getContentSession();
        when(policyWidget.getPolicy()).thenReturn(parentPolicy);
        when(contentSession.getPolicyCMServer()).thenReturn(cmServer);
        when(cmServer.getCurrentCaller()).thenReturn(caller);
        when(cmServer.createContentVersion(any(VersionedContentId.class))).thenReturn(policy);
        when(userId.getPrincipalIdString()).thenReturn(USER_PRINCIPAL);
    }

    @Test
    public void mustBeTwittableInputPolicy() throws CMException {
        when(policyWidget.getPolicy()).thenReturn(mock(Policy.class));
        PrepareResult result = target.validateSelf();
        verify(result).setError(true);
        verify(target).getString(OTwitterAccountSelectWidget.MUST_BE_TWITTABLE_INPUT_POLICY);
    }

    @Test
    public void doNotValidateWhenCheckboxIsNotChecked() throws CMException {
        PrepareResult result = target.validateSelf();
        verify(result, never()).setError(anyBoolean());
        verify(result, never()).setLocalizeMessage(anyString());
    }

    @Test
    public void validateErrorWhenNoTwitterAccountIsSelected() throws CMException {
        when(parentPolicy.isEnabled()).thenReturn(true);
        PrepareResult result = target.validateSelf();
        verify(result).setError(true);
        verify(result).setLocalizeMessage(OTwitterAccountSelectWidget.ACCOUNT_REQUIRED);
    }

    @Test
    public void validateErrorWhenNotTwitterAccountIsSelected() throws CMException {
        ContentId contentId = mock(ContentId.class);
        when(parentPolicy.isEnabled()).thenReturn(true);
        when(singleReference.getReference()).thenReturn(contentId);
        doReturn(false).when(target).isInstanceOfTwitterAccount(contentId);
        PrepareResult result = target.validateSelf();
        verify(result).setError(true);
        verify(result).setLocalizeMessage(OTwitterAccountSelectWidget.MUST_BE_TWITTER_ACCOUNT);
    }

    @Test
    public void noValidationErrorWhenAllConditionsStatified() throws CMException {
        ContentId contentId = mock(ContentId.class);
        when(parentPolicy.isEnabled()).thenReturn(true);
        when(singleReference.getReference()).thenReturn(contentId);
        doReturn(true).when(target).isInstanceOfTwitterAccount(contentId);
        PrepareResult result = target.validateSelf();
        verify(result, never()).setError(anyBoolean());
        verify(result, never()).setLocalizeMessage(anyString());
    }

    @Test
    public void shouldAbleSetDefaultAccount() throws OrchidException, CMException {
        target.setOContentSingleSelect(contentSelect);
        Policy user = mock(Policy.class);
        ContentId twitterAccount = mock(ContentId.class);
        when(user.getContentReference("twitterAccount")).thenReturn(twitterAccount);
        when(cmServer.getPolicy(any(ContentId.class))).thenReturn(user, mock(MockTwitterAccount.class));
        target.selectDefaultAccount(oc);
        verify(contentSelect).setSelectedContentId(any(ContentId.class), any(OrchidContext.class));
    }
    
    @Test
    public void shouldNotSetDefaultAccount() throws OrchidException, CMException {
        target.setOContentSingleSelect(contentSelect);
        when(contentSelect.getSelectedContentId()).thenReturn(mock(ContentId.class));
        target.selectDefaultAccount(oc);
        verify(contentSelect, never()).setSelectedContentId(any(ContentId.class), any(OrchidContext.class));
    }

    private interface MockSingleReference extends SingleReference, Policy {}

    private interface MockTwitterAccount extends TwitterAccount, Policy {}
}
