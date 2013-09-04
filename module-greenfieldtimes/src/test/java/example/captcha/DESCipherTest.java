package example.captcha;

import junit.framework.TestCase;

public class DESCipherTest extends TestCase {

    public static final String TO_TEST = "to test abc [] {/=}";
    
    public void testCipher() throws Exception {
        
        String secretKey = DESCipher.generateSecretKey();
        DESCipher cipher1 = new DESCipher(secretKey);
        DESCipher cipher2 = new DESCipher(secretKey);
        
        String encrypted1 = cipher1.encrypt(TO_TEST);
        String encrypted2 = cipher2.encrypt(TO_TEST);
        
        String decrypted1 = cipher2.decrypt(encrypted1);
        assertEquals(TO_TEST, decrypted1);
        
        String decrypted2 = cipher1.decrypt(encrypted2);
        assertEquals(TO_TEST, decrypted2);
    }
    
}
