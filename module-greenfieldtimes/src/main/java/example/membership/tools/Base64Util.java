package example.membership.tools;

import java.io.UnsupportedEncodingException;

import com.polopoly.util.Base64;

/**
 * Utility class used to encode / decode string to / from
 * their base64-representations.
 */
public class Base64Util
{
    private final String _charsetName;
    
    /**
     * Using the default utf-8 charset.
     */
    public Base64Util()
    {
        this("UTF-8");
    }
    
    public Base64Util(String charsetName)
    {
        _charsetName = charsetName;
    }
    
    /**
     * Decode the given base64-encoded string.
     * 
     * @param source the encoded string
     * @return the decoded string or null if input source is null
     * 
     * @throws UnsupportedEncodingException
     */
    public String decode(String source)
        throws UnsupportedEncodingException
    {
        return source != null ? new String(Base64.decode(padIfNecessary(source)),
                                           _charsetName)
                              : null;
    }
    
    /**
     * Encode the given string to base64.
     * 
     * @param source the string to encode
     * @return the encoded string (minus possible trailing '=' characters) or
     *         null if input source is null
     * 
     * @throws UnsupportedEncodingException
     */
    public String encode(String source)
        throws UnsupportedEncodingException
    {
        return source != null ? Base64.encodeBytes(source.getBytes(_charsetName),
                                                   Base64.DONT_BREAK_LINES)
                              : null;
    }
    
    /**
     * Add = to end of stripped base64 to make valid base64.
     */
    private String padIfNecessary(String base64String)
    {
        // Tomcat can't deal with '=' in cookies (and will have removed then),
        // so we restore if needed.
        if (base64String == null || base64String.length() % 4 == 0) {
            return base64String;
        }
        
        // Need to add padding
        char[] padded = new char[base64String.length() + 4 - base64String.length() % 4];
        char[] original = base64String.toCharArray();
        System.arraycopy(original, 0, padded, 0, original.length);
        for (int i = base64String.length(); i < padded.length; i++) {
            padded[i] = '=';
        }

        return new String(padded);
    }
}
