package example.content.editorialblog;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An iterator splitter that splits on a regexp.
 */
public class RegexTokenizer implements Iterator<Object> {

    private CharSequence input;

    private Matcher matcher;

    private boolean returnDelims;

    private String delim;

    private String match;

    private int lastEnd = 0;

    /**
     * An iterator splitter that splits on a regexp.
     * 
     * @param input the string to split.
     * @param patternStr a regular expression pattern that identifies tokens.
     * @param returnDelims if true then the delimiters are also returned.
     */
    public RegexTokenizer(CharSequence input,
                          String patternStr,
                          boolean returnDelims) {
        
        this.input = input;
        this.returnDelims = returnDelims;

        Pattern pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(input);
    }

    /**
     * Standard iterator method.
     *
     * @return true if there is another element available.
     */
    public boolean hasNext() {
        if (matcher == null) {
            return false;
        }
        if (delim != null || match != null) {
            return true;
        }
        if (matcher.find()) {
            if (returnDelims) {
                delim = input.subSequence(lastEnd, matcher.start()).toString();
            }
            match = matcher.group();
            lastEnd = matcher.end();
        } else if (returnDelims && lastEnd < input.length()) {
            delim = input.subSequence(lastEnd, input.length()).toString();
            lastEnd = input.length();
            matcher = null;
        }
        return delim != null || match != null;
    }

    /**
     * Standard iterator method.
     *
     * @return the next object
     */
    public Object next() {
        String result = null;

        if (delim != null) {
            result = delim;
            delim = null;
        } else if (match != null) {
            result = match;
            match = null;
        }
        return result;
    }

    /**
     * Returns the next available token as a String.
     * Convenience method to get the next token as a String.
     * 
     * @return the next token as a String
     */
    public String nextToken() {
        return (String)next();
    }

    /**
     * Checks whether or not the next token is a delimiting
     * expression or an actual value.
     * 
     * @return true if the next token is a delimiter.
     */
    public boolean isNextDelim() {
        return delim != null && match == null;
    }

    /**
     * Checks whether the next token is is a valid token
     * and not a delimiting string.
     *
     * @return true if the next token is valid.
     */
    public boolean isNextToken() {
        return delim == null && match != null;
    }

    /**
     * Not implemented overridden method.
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
