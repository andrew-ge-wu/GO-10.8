package example.akismet;

import javax.servlet.http.HttpServletRequest;

/**
 * Akismet Client for the <a href="http://akismet.com/development/api/">Akismet
 * API</a> documentation.
 */
public interface AkismetClient {

    /** "Unknown" comment type */
    public static final String COMMENT_TYPE_BLANK = "";

    /** "Comment" comment type */
    public static final String COMMENT_TYPE_COMMENT = "comment";

    /** "Trackback" comment type */
    public static final String COMMENT_TYPE_TRACKBACK = "trackback";

    /** "Pingback" comment type */
    public static final String COMMENT_TYPE_PINGBACK = "pingback";

    /**
     * From the API docs, The key verification call should be made before
     * beginning to use the service. It requires two variables, key and blog.
     * 
     * @return <code>true</code> if the API key has been verified, else
     *         <code>false</code>.
     * @throws AkismetException
     *             If e.g. failed to connect Akismet service.
     */
    public boolean verifyAPIKey() throws AkismetException;
    
    /**
     * From the API docs, This is basically the core of everything. This call
     * takes a number of arguments and characteristics about the submitted
     * content and then returns a thumbs up or thumbs down. Almost everything is
     * optional, but performance can drop dramatically if you exclude certain
     * elements. I would recommend erring on the side of too much data, as
     * everything is used as part of the Akismet signature."
     * 
     * @param request
     *            The commenter request
     * @param permalink
     *            The permanent location of the entry the comment was submitted
     *            to
     * @param commentType
     *            May be blank, comment, trackback, pingback, or a made up value
     *            like "registration"
     * @param author
     *            Submitted name with the comment
     * @param authorEmail
     *            Submitted email address
     * @param authorURL
     *            Commenter URL
     * @param commentContent
     *            The content that was submitted
     * @return <code>true</code> if the comment is identified by Akismet as
     *         spam, <code>false</code> otherwise.
     * @throws AkismetException
     *             If e.g. failed to connect Akismet service.
     */
    public boolean commentCheck(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent) throws AkismetException;

    /**
     * From the API docs, This call is for submitting comments that weren't
     * marked as spam but should have been. It takes identical arguments as
     * comment check."
     * 
     * @param request
     *            The commenter request
     * @param permalink
     *            The permanent location of the entry the comment was submitted
     *            to
     * @param commentType
     *            May be blank, comment, trackback, pingback, or a made up value
     *            like "registration"
     * @param author
     *            Submitted name with the comment
     * @param authorEmail
     *            Submitted email address
     * @param authorURL
     *            Commenter URL
     * @param commentContent
     *            The content that was submitted
     * @throws AkismetException
     *             If e.g. failed to connect Akismet service.
     */
    public void submitSpam(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent) throws AkismetException;

    /**
     * From the API docs, This call is intended for the marking of false
     * positives, things that were incorrectly marked as spam. It takes
     * identical arguments as comment check and submit spam."
     * 
     * @param request
     *            The commenter request
     * @param permalink
     *            The permanent location of the entry the comment was submitted
     *            to
     * @param commentType
     *            May be blank, comment, trackback, pingback, or a made up value
     *            like "registration"
     * @param author
     *            Submitted name with the comment
     * @param authorEmail
     *            Submitted email address
     * @param authorURL
     *            Commenter URL
     * @param commentContent
     *            The content that was submitted
     * @throws AkismetException
     *             If e.g. failed to connect Akismet service.
     */
    public void submitHam(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent) throws AkismetException;

}
