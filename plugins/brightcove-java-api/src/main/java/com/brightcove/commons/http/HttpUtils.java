package com.brightcove.commons.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * <p>
 *    A simple class to help working with HTTP responses
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">three4clavin</a>
 *
 */
public class HttpUtils {
    
    /**
     * The default buffer size to use.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    
	/**
	 * <p>
	 *    Parses an HTTP request into a into a plain String response
	 * </p>
	 * 
	 * @param entity HTTP entity to read from and parse
	 * @return String with the HTTP response contents
	 * @throws IOException If the entity can't be parsed
	 * @throws IllegalStateException If something went really wrong reading from the server
	 */
	public static String parseHttpEntity(HttpEntity entity) throws IOException, IllegalStateException {
		if(entity == null){
			return null;
		}
		
		String      output   = "";
		InputStream instream = entity.getContent();
		String      charSet  = "UTF-8";
		
		Header header = entity.getContentType();
		if(header != null){
			charSet = header.getValue();
			
			String charSetUpper = charSet.toUpperCase();
			int    charSetIdx   = charSetUpper.indexOf("CHARSET");
			
			if(charSetIdx < 0){
				charSet = "UTF-8";
			}
			else{
				charSet = charSet.substring(charSetIdx + "charset".length());
				charSet = charSet.split("=")[1].trim();
			}
		}
		
		output = inputStreamtoString(instream, charSet);
		
	    return output;
	}
	
	private static String inputStreamtoString(InputStream input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        if (encoding == null) {
            copy(input, sw);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, sw);
        }
        return sw.toString();
	}
    
    private static void copy(InputStream input, Writer output)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }
    
    private static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
