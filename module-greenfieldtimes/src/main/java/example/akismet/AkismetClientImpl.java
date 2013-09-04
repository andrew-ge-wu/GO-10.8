package example.akismet;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * A Akismet REST (API version 1.1) implementation. The implementation does not
 * implement the "other" parameter (other server environment variables), as
 * defined by the API as:
 * 
 * <pre>
 * In PHP there is an array of environment variables called $_SERVER which
 * contains information about the web server itself as well as a key/value
 * for every HTTP header sent with the request. This data is highly useful
 * to Akismet as how the submitted content interacts with the server can be
 * very telling, so please include as much information as possible.
 * </pre>
 */
public class AkismetClientImpl implements AkismetClient {

    private static final Logger LOG = Logger.getLogger(AkismetClientImpl.class.getName());

    protected static final String VERIFY_API_KEY_SUCCESS_RESPONSE = "valid";
    
    protected static final String VERIFY_API_KEY_FAILURE_RESPONSE = "invalid";

    protected static final String METHOD_VERIFY_KEY = "verify-key";
    
    protected static final String METHOD_COMMENT_CHECK = "comment-check";

    protected static final String METHOD_SUBMIT_HAM = "submit-ham";

    protected static final String METHOD_SUBMIT_SPAM = "submit-spam";

    protected static final String HTTP_REFERER = "Referer";

    protected static final String USER_AGENT_HEADER = "User-Agent";

    protected static final String USER_AGENT_VALUE = "Polopoly Akismet Client/1.0";

    protected static final String API_PARAMETER_KEY = "key";

    protected static final String API_PARAMETER_BLOG = "blog";

    protected static final String API_PARAMETER_USER_IP = "user_ip";

    protected static final String API_PARAMETER_USER_AGENT = "user_agent";

    protected static final String API_PARAMETER_REFERRER = "referrer";

    protected static final String API_PARAMETER_PERMALINK = "permalink";

    protected static final String API_PARAMETER_COMMENT_TYPE = "comment_type";

    protected static final String API_PARAMETER_COMMENT_AUTHOR = "comment_author";

    protected static final String API_PARAMETER_COMMENT_AUTHOR_EMAIL = "comment_author_email";

    protected static final String API_PARAMETER_COMMENT_AUTHOR_URL = "comment_author_url";

    protected static final String API_PARAMETER_COMMENT_CONTENT = "comment_content";
    
    private String akismetBaseUrl = null;

    private HttpClient httpClient;

    private String apiKey;

    private String blog;

    private Semaphore connectionAvailable;

    /**
     * Construct an instance to work with the Akismet API.
     * 
     * @param apiKey
     *            Akismet API key
     * @param blog
     *            Blog associated with the API key
     * @param proxyHost
     *            Proxy host, or <code>null</code> if no proxy configuration is
     *            required.
     * @param proxyPort
     *            Proxy port. If no proxy configuration is required (i.e.
     *            <code>proxyHost</code> is <code>null</code>) then this value
     *            is undefined.
     * @param proxyUsername
     *            Username to access proxy, or <code>null</code> if no proxy
     *            credentials configuration is required.
     * @param proxyPassword
     *            Password to access proxy, or <code>null</code> if no proxy
     *            credentials configuration is required.
     * @param socketTimeOut
     *            The default socket timeout (SO_TIMEOUT) in milliseconds which
     *            is the timeout for waiting for data. A timeout value of zero
     *            is interpreted as an infinite timeout.
     * @param connectionAvailable
     * @throws IllegalArgumentException
     *             If either the API key or blog is <code>null</code>
     */
    public AkismetClientImpl(String apiKey, String blog, String proxyHost,
                             int proxyPort, String proxyUsername, String proxyPassword,
                             int socketTimeOut, int connectionTimeout,
                             Semaphore connectionAvailable)
        throws IllegalArgumentException
    {
        this.apiKey = apiKey;
        this.blog = blog;
        this.connectionAvailable = connectionAvailable;

        if (apiKey == null) {
            throw new IllegalArgumentException("API key cannot be null");
        }

        if (blog == null) {
            throw new IllegalArgumentException("Blog cannot be null");
        }
        
        if (connectionAvailable == null) {
            throw new IllegalArgumentException("Connection semaphore cannot be null");
        }

        httpClient = new HttpClient();
        HttpClientParams httpClientParams = new HttpClientParams();
        DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(
                0, false);
        
        httpClientParams.setParameter(USER_AGENT_HEADER, USER_AGENT_VALUE);
        httpClientParams.setParameter(HttpClientParams.RETRY_HANDLER,
                defaultHttpMethodRetryHandler);
        
        httpClient.setParams(httpClientParams);

        HttpConnectionManager httpConnectionManager =
            new SimpleHttpConnectionManager();
        
        // create connection defaults
        HttpConnectionManagerParams connectionManagerParams =
            new HttpConnectionManagerParams();
        connectionManagerParams.setSoTimeout(socketTimeOut);
        connectionManagerParams.setConnectionTimeout(connectionTimeout);
        httpConnectionManager.setParams(connectionManagerParams);
        
        httpClient.setHttpConnectionManager(httpConnectionManager);
                
        // Proxy configuration
        if (proxyHost != null) {
            HostConfiguration hostConfiguration = new HostConfiguration();
            hostConfiguration.setProxy(proxyHost, proxyPort);
            httpClient.setHostConfiguration(hostConfiguration);
        }

        // Proxy credentials
        if (proxyUsername != null && proxyPassword != null) {
            httpClient.getState().setProxyCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(proxyUsername,
                            proxyPassword));
        }
    }
    
    /**
     * Get the base URL for the Akismet REST service.
     * 
     * @return The Akismet base URL
     */
    public String getAkismetBaseUrl() {
        if (akismetBaseUrl == null) {
            akismetBaseUrl = "http://" + apiKey + ".rest.akismet.com/1.1/";
        }
        return akismetBaseUrl;
    }

    /**
     * Set the base URL for the Akismet REST service.
     * <p>
     * <b>Note</b>: The Akismet URL usually contains the API key.
     * </p>
     * @param url
     *            the Akismet base URL
     */
    public void setAkismetBaseUrl(String url) {
        String suffix = "";
        if (!url.endsWith("/")) {
            suffix = "/";
        }
        akismetBaseUrl = url + suffix;
    }

    /**
     * From the API docs, The key verification call should be made before
     * beginning to use the service. It requires two variables, key and blog.
     * 
     * @return <code>true</code> if the API key has been verified, else
     *         <code>false</code>.
     * @throws AkismetException
     *             If e.g. failed to connect Akismet service.
     */
    public boolean verifyAPIKey()
        throws AkismetException
    {
        PostMethod post = new PostMethod(getAkismetBaseUrl() + METHOD_VERIFY_KEY);
        
        post.addParameter(API_PARAMETER_KEY, apiKey);
        post.addParameter(API_PARAMETER_BLOG, blog);

        String response = null;
        
        try {
            response = executeMethod(post);
        } catch (AkismetTooManyConnectionsException e) {
            throw new AkismetException("Cannot authenticate. No available threads.", e);
        }
        
        if (VERIFY_API_KEY_SUCCESS_RESPONSE.equals(response)) {
            return true;
        }
        
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeexample.akismet.AkismetClient#commentCheck(javax.servlet.http.
     * HttpServletRequest, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean commentCheck(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent)
        throws AkismetException
    {
        String response = null;
        
        try {
            response = call(request, METHOD_COMMENT_CHECK, permalink,
                    commentType, author, authorEmail, authorURL, commentContent);
        } catch (AkismetTooManyConnectionsException e) {
            LOG.log(Level.WARNING, "Too many simultaneous requests to Akismet server. " +
                                   "Comment allowed.");
            
            return false; // false == is not spam
        }
        
        if (VERIFY_API_KEY_FAILURE_RESPONSE.equals(response)) {
            throw new AkismetException("Invalid Akismet key ('" +
                    apiKey + "') used.");
        }
        
        return Boolean.parseBoolean(response);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * example.akismet.AkismetClient#submitHam(javax.servlet.http.HttpServletRequest
     * , java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void submitHam(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent) throws AkismetException {

        try {
            call(request, METHOD_SUBMIT_HAM, permalink, commentType, author,
                    authorEmail, authorURL, commentContent);
        } catch (AkismetTooManyConnectionsException e) {
            LOG.log(Level.WARNING, "Too many simultaneous requests to Akismet server. " +
                    "Dropping ham submissions.");
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @seeexample.akismet.AkismetClient#submitSpam(javax.servlet.http.
     * HttpServletRequest, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public void submitSpam(HttpServletRequest request, String permalink,
            String commentType, String author, String authorEmail,
            String authorURL, String commentContent) throws AkismetException {

        try {
            call(request, METHOD_SUBMIT_SPAM, permalink, commentType, author,
                    authorEmail, authorURL, commentContent);
        } catch (AkismetTooManyConnectionsException e) {
            LOG.log(Level.WARNING, "Too many simultaneous requests to Akismet server. " +
                    "Dropping spam submissions.");
        }
    }

    protected String call(HttpServletRequest request, String function,
            String permalink, String commentType, String author,
            String authorEmail, String authorURL, String commentContent)
        throws AkismetException, AkismetTooManyConnectionsException
    {
        String ipAddress = request.getRemoteAddr();
        String referrer = request.getHeader(HTTP_REFERER);
        String userAgent = request.getHeader(USER_AGENT_HEADER);
        
        return call(function, ipAddress, userAgent, referrer, permalink,
                commentType, author, authorEmail, authorURL, commentContent,
                null);
    }

    protected String call(String function, String ipAddress, String userAgent,
            String referrer, String permalink, String commentType,
            String author, String authorEmail, String authorURL,
            String commentContent, Map<String, String> other)
        throws AkismetException, AkismetTooManyConnectionsException
    {
        String akismetURL = getAkismetBaseUrl() + function;

        PostMethod post = new PostMethod(akismetURL);
        post.addRequestHeader("Content-Type",
                "application/x-www-form-urlencoded; charset=utf-8");

        // Required fields
        post.addParameter(new NameValuePair(API_PARAMETER_BLOG, blog));
        post.addParameter(new NameValuePair(API_PARAMETER_USER_IP,
                ipAddress != null ? ipAddress : ""));
        post.addParameter(new NameValuePair(API_PARAMETER_USER_AGENT,
                userAgent != null ? userAgent : ""));

        // Optional fields
        if (referrer != null) {
            post.addParameter(new NameValuePair(API_PARAMETER_REFERRER,
                    referrer));
        }
        if (permalink != null) {
            post.addParameter(new NameValuePair(API_PARAMETER_PERMALINK,
                    permalink));
        }
        if (commentType != null) {
            post.addParameter(new NameValuePair(API_PARAMETER_COMMENT_TYPE,
                    commentType));
        }
        if (author != null) {
            post.addParameter(new NameValuePair(API_PARAMETER_COMMENT_AUTHOR,
                    author));
        }
        if (authorEmail != null) {
            post.addParameter(new NameValuePair(
                    API_PARAMETER_COMMENT_AUTHOR_EMAIL, authorEmail));
        }
        if (authorURL != null) {
            post.addParameter(new NameValuePair(
                    API_PARAMETER_COMMENT_AUTHOR_URL, authorURL));
        }
        if (commentContent != null) {
            post.addParameter(new NameValuePair(API_PARAMETER_COMMENT_CONTENT,
                    commentContent));
        }

        if (other != null && other.size() > 0) {
            Iterator<String> keyIterator = other.keySet().iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                if (key != null && other.get(key) != null) {
                    post.addParameter(new NameValuePair(key, other.get(key)));
                }
            }
        }

       return executeMethod(post);
    }

    private String executeMethod(PostMethod post)
        throws AkismetException, AkismetTooManyConnectionsException
    {
        boolean aquiredPermit = false;
        try {
            aquiredPermit = connectionAvailable.tryAcquire();
            if (aquiredPermit) {
                try {
                    int responseCode = httpClient.executeMethod(post);
                    String result = post.getResponseBodyAsString();
                    
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        String msg = post.getPath()
                                + " failed with repsonse code: " + responseCode;
                        LOG.log(Level.WARNING, msg);
                        throw new AkismetException(msg);
                    }
                    
                    return result.trim();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to execute Akismet method: "
                            + post.getPath() + ": " + e.getMessage());
                    throw new AkismetException(e.getMessage(), e);
                }
            } else {
                throw new AkismetTooManyConnectionsException();
            }
        } finally {
            if (aquiredPermit) {
                connectionAvailable.release();
            }
        }
    }
}
