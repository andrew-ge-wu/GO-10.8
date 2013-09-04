package example.membership;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;

import com.polopoly.user.server.User;

import example.MockitoBase;
import example.captcha.ClusteredImageCaptchaService;

public class ActionResetPasswordTest extends MockitoBase {

    private static final String TEST_SITE = "gt";
    
    private static final String TEST_USER_NAME = "test@polopoly.com";

    private static final String TEST_REAL_USER_NAME = TEST_SITE + "_" + TEST_USER_NAME; 
    
    private static final String TEST_MSG_ID = "THE_MESSAGE_ID";

    private static final String TEST_REFERRER = "REFERRER";

    @Mock
    UserHandler userHandler;
    
    @Mock
    MembershipSettings membershipSettings;
    
    @Mock
    ClusteredImageCaptchaService captchaService;
    
    @Mock
    ResetPasswordMailService mailService;
    
    @Mock
    User testUser;
    
    PasswordService passwordService;
    
    ActionResetPassword toTest;
    
    @Override
    protected void setUp() throws Exception {
        
        super.setUp();
        
        passwordService = new PasswordService(1, "a");
        
        toTest = new ActionResetPassword(userHandler, 
                                         membershipSettings, 
                                         passwordService, 
                                         captchaService);
    }    
    
    public void testResetPassword() throws Exception {
        
        when(userHandler.getUserByLoginName(TEST_REAL_USER_NAME)).thenReturn(testUser);
        when(mailService.send(TEST_USER_NAME, "a")).thenReturn(TEST_MSG_ID);
        when(membershipSettings.getResetPasswordMailService(TEST_SITE)).thenReturn(mailService);
     
        toTest.doResetPassword(TEST_USER_NAME, TEST_REAL_USER_NAME, TEST_SITE, TEST_REFERRER);
        
        verify(mailService).send(TEST_USER_NAME, "a");
    }    
}
