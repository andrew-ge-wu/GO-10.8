package example.membership.tools;

import junit.framework.TestCase;

import com.polopoly.util.Base64;

public class Base64UtilTest extends TestCase {
    
    private static final String TEST_STRING_BASE64_NO_PADDED = "string";
    private static final String TEST_STRING_BASE64_ONE_PADDED = "string12";
    private static final String TEST_STRING_BASE64_TWO_PADDED = "string1";

    private static final String CHARSET_NAME = "UTF-8";
    
    private Base64Util toTest;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        toTest = new Base64Util(CHARSET_NAME);
    }
    
    public void testEncodeNoPaddingString()
        throws Exception
    {
        String encoded = toTest.encode(TEST_STRING_BASE64_NO_PADDED);
        String verify = Base64.encodeBytes(TEST_STRING_BASE64_NO_PADDED.getBytes(CHARSET_NAME),
                                           Base64.DONT_BREAK_LINES);
        assertEquals("Expected to encode correctly with no padding", verify, encoded);
    }
    
    public void testEncodePaddingString()
        throws Exception
    {
        String encoded = toTest.encode(TEST_STRING_BASE64_ONE_PADDED);
        String verify = Base64.encodeBytes(TEST_STRING_BASE64_ONE_PADDED.getBytes(CHARSET_NAME),
                                           Base64.DONT_BREAK_LINES);
        assertEquals("Expected to encode correctly with one pad", verify, encoded);
        
        encoded = toTest.encode(TEST_STRING_BASE64_TWO_PADDED);
        verify = Base64.encodeBytes(TEST_STRING_BASE64_TWO_PADDED.getBytes(CHARSET_NAME),
                                    Base64.DONT_BREAK_LINES);
        assertEquals("Expected to encode correctly with two pads", verify, encoded);
    }

    public void testDecodeCorrectBase64EncodedString() 
        throws Exception
    {
        // Base64 encoded with no padding 
        String encoded = Base64.encodeBytes(
                TEST_STRING_BASE64_NO_PADDED.getBytes(CHARSET_NAME),
                Base64.DONT_BREAK_LINES);
        String decoded = toTest.decode(encoded);
        assertEquals("Expected correct Base64 decoded string", 
                     TEST_STRING_BASE64_NO_PADDED,
                     decoded);
        
        // Base64 encoded with one padding (=)
        encoded = Base64.encodeBytes(
                TEST_STRING_BASE64_ONE_PADDED.getBytes(CHARSET_NAME),
                Base64.DONT_BREAK_LINES);
        decoded = toTest.decode(encoded);
        assertEquals("Expected correct Base64 decoded string", 
                     TEST_STRING_BASE64_ONE_PADDED,
                     decoded);
        
        // Base64 encoded with two padding (==)
        encoded = Base64.encodeBytes(
                TEST_STRING_BASE64_TWO_PADDED.getBytes(CHARSET_NAME),
                Base64.DONT_BREAK_LINES);
        decoded = toTest.decode(encoded);
        assertEquals("Expected correct Base64 decoded string", 
                     TEST_STRING_BASE64_TWO_PADDED,
                     decoded);
    }
    
    public void testDedcodeStrippedBase64EncodedString() 
        throws Exception
    {
        // Base64 encoded with one padding (=)
        String encoded = Base64.encodeBytes(
                TEST_STRING_BASE64_ONE_PADDED.getBytes(CHARSET_NAME),
                Base64.DONT_BREAK_LINES);
        
        String stripped = stripBase64Padding(encoded);
        String decoded = toTest.decode(stripped);
        assertEquals("Expected correct Base64 decoded string", 
                     TEST_STRING_BASE64_ONE_PADDED,
                     decoded);
        
        // Base64 encoded with two padding (==)
        encoded = Base64.encodeBytes(
                TEST_STRING_BASE64_TWO_PADDED.getBytes(CHARSET_NAME),
                Base64.DONT_BREAK_LINES);
        stripped = stripBase64Padding(encoded);
        decoded = toTest.decode(stripped);
        assertEquals("Expected correct Base64 decoded string", 
                     TEST_STRING_BASE64_TWO_PADDED,
                     decoded);
    }
    
    public void testNullSafety() throws Exception {
        assertNull(toTest.decode(null));
        assertNull(toTest.encode(null));
    }
    
    /**
     * Strip base64 string from =.
     */
    private String stripBase64Padding(String base64)
    {
        int padding = base64.indexOf('=');
        if (padding == -1) {
            return base64;
        }
        
        return base64.substring(0, padding);
    }
    
}
