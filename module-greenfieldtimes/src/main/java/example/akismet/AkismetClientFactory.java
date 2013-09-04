package example.akismet;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.servlet.ServletContext;

import com.polopoly.application.Application;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ManagedBeanRegistry;
import com.polopoly.management.jmx.JMXManagedBeanName;

/**
 * Factory to create Akismet clients.
 */
public class AkismetClientFactory {

    private static final Logger LOG = Logger.getLogger(AkismetClientFactory.class.getName());
    
    private final String proxyHost;

    private final int proxyPort;

    private final String proxyUsername;

    private final String proxyPassword;

    private int socketTimeOut;
    
    private int connectionTimeout;

    private String apiKey;
    
    private static final int DEFAULT_MAX_PERMITS = 10;
    
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    
    private static volatile AkismetClientFactory clientFactory;
    
    private Semaphore connectionAvailable;

    private final int maxPermits;
   
    /**
     * Creates a new factory for Akismet clients.
     * 
     * @param proxyHost
     *            Proxy host, or <code>null</code> if no proxy configuration is
     *            required.
     * @param proxyPort
     *            Proxy port. If no proxy configuration is required (i.e.
     *            <code>proxyHost</code> is <code>null</code>) then this value
     *            is undefined.
     * @param proxyUsernameproxyUsername
     *            Username to access proxy, or <code>null</code> if no proxy
     *            credentials configuration is required.
     * @param proxyPassword
     *            Password to access proxy, or <code>null</code> if no proxy
     *            credentials configuration is required.
     * @param socketTimeOut
     *            The default socket timeout (SO_TIMEOUT) in milliseconds which
     *            is the timeout for waiting for data. A timeout value of zero
     *            is interpreted as an infinite timeout.
     * @param connectionTimeout
     *            Sets the timeout in milliseconds until a connection is
     *            established. A value of zero means the timeout is not used.
     * @param apiKey
     *            The Akismet API key (for the current Akismet account)
     */
    private AkismetClientFactory(String proxyHost, int proxyPort,
                                 String proxyUsername, String proxyPassword,
                                 int socketTimeOut, int connectionTimeout,
                                 String apiKey, int maxPermits)
    {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.socketTimeOut = socketTimeOut;
        this.connectionTimeout = connectionTimeout;
        this.apiKey = apiKey;
        this.maxPermits = maxPermits;
        
        connectionAvailable = new Semaphore(maxPermits, true);
    }
     
    public static AkismetClientFactory getInstance(ServletContext servletContext, boolean registerMBean)
        throws CMException
    {
        if (clientFactory == null) {
            synchronized (AkismetClientFactory.class) {
                if (clientFactory == null) {
                    
                    Application application = ApplicationServletUtil
                            .getApplication(servletContext);
                    
                    CmClientBase client = (CmClientBase) application
                            .getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
                    PolicyCMServer cmServer = client.getPolicyCMServer();
                    
                    ContentRead akismetClientConfig = cmServer
                        .getContent(new ExternalContentId("example.AkismetSettingsConfig"));
                    
                    String proxyHost =
                        akismetClientConfig.getComponent("proxyHost", "value");
                    if (proxyHost != null && "".equals(proxyHost.trim())) {
                        proxyHost = null;
                    }
                    String proxyUserName =
                        akismetClientConfig.getComponent("proxyUserName", "value");
                    if (proxyUserName != null && "".equals(proxyUserName.trim())) {
                        proxyUserName = null;
                    }
                    String proxyPassword =
                        akismetClientConfig.getComponent("proxyPassword", "value");
                    if (proxyPassword != null && "".equals(proxyPassword.trim())) {
                        proxyPassword = null;
                    }
                    
                    int proxyPort =
                        getAsInt(akismetClientConfig.getComponent("proxyPort", "value"), 0);
                    int socketTimeout =
                        getAsInt(akismetClientConfig.getComponent("socketTimeout", "value"),
                                DEFAULT_SOCKET_TIMEOUT);
                    int connectionTimeout =
                        getAsInt(akismetClientConfig.getComponent("connectionTimeout", "value"),
                                DEFAULT_CONNECTION_TIMEOUT);
                    
                    String apiKey =
                        akismetClientConfig.getComponent("apiKey", "value");

                    int maxPermits = getAsInt(akismetClientConfig.getComponent("maxPermits", "value"),
                            DEFAULT_MAX_PERMITS);
                    
                    clientFactory =
                        new AkismetClientFactory(proxyHost, proxyPort,
                                                 proxyUserName, proxyPassword,
                                                 socketTimeout, connectionTimeout,
                                                 apiKey, maxPermits);
                    
                    // Register semaphore mBean
                    if (registerMBean) {
                        ManagedBeanRegistry mBeanRegistry = application
                                .getManagedBeanRegistry();
    
                        
                        JMXManagedBeanName mBeanName;
                        
                        try {
                            mBeanName = new JMXManagedBeanName("example", "name", "AkismetClientManager");
                        }
                        catch (MalformedObjectNameException e) {
                            throw new RuntimeException(e);
                        }
                        
                        // Unregister any previous mBean
                        try {
                            mBeanRegistry.unregisterManagedBean(mBeanName);
                        } catch (Exception e) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "No previous semaphore mbean " +
                                        "for akismet clients to unregister");
                            }
                        }
                        
                        // Register the mBean
                        AkismetClientFactoryMBean mBean = new AkismetClientFactoryMBeanImpl(
                                clientFactory.connectionAvailable, clientFactory);
                        try {
                            mBeanRegistry.registerManagedBean(mBeanName,
                                                              mBean,
                                                              AkismetClientFactoryMBean.class);
                        } catch (Exception e) {
                            LOG.log(Level.WARNING, "Unable to register semaphore "
                                    + "mbean for akismet clients", e);
                        }
                    }
                }
            }
        }
        return clientFactory;
    }
    
    /**
     * Checks if API key has been configured and is in use.
     * A client may still be used without this being set, if they override the API key.
     * 
     * @return true if it does. false if not.
     */
    public boolean hasAPIKey()
    {
        return apiKey != null;
    }
    
    /**
     * Return the time to wait at most for an Akismet HTTP request.
     * 
     * @return timeout value in millisec.
     */
    int getSocketTimeout() {
        return socketTimeOut;
    }

    /**
     * Set the time to wait at most for an Akismet HTTP request.
     * 
     * @param timeout
     *            timeout value in millisec.
     */
    void setSocketTimeout(int timeout) {
        socketTimeOut = timeout;
    }
    
    /**
     * Return the time to wait at most for establishing an Akismet HTTP request.
     * 
     * @return timeout value in millisec.
     */
    int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Set the time to wait at most for establishing an Akismet HTTP request.
     * 
     * @param timeout
     *            timeout value in millisec.
     */
    void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }
     
    /**
     * Get a new Akismet client associated with the given blog (with the API key
     * given in the configuration.)
     * 
     * @param siteUrl
     *            The front page or home URL of the instance making the request.
     *            For a blog or wiki this would be the front page. Note: Must be
     *            a full URI, including http://.
     * 
     * @throws IllegalArgumentException
     *             If either the API key or blog is <code>null</code> or
     *             invalid.
     * @throws AkismetException
     *             If failed to connect or request Akismet service.
     */
    public AkismetClient getClient(String siteUrl)
        throws IllegalArgumentException, AkismetException
    {
        return getClient(apiKey, siteUrl);
    }
    
    /**
     * Get a new Akismet client associated with the given blog (with a different
     * API key than the one specified.)
     * 
     * @param key
     *            A separate Akismet API key, used instead of the default one.
     * @param siteUrl
     *            The front page or home URL of the instance making the request.
     *            For a blog or wiki this would be the front page. Note: Must be
     *            a full URI, including http://.
     * 
     * @throws IllegalArgumentException
     *             If either the API key or blog is <code>null</code> or
     *             invalid.
     * @throws AkismetException
     *             If failed to connect or request Akismet service.
     */
    public AkismetClient getClient(String key, String siteUrl)
        throws IllegalArgumentException, AkismetException
    {
        AkismetClientImpl client =
            new AkismetClientImpl(key, siteUrl, proxyHost,
                                  proxyPort, proxyUsername, proxyPassword,
                                  socketTimeOut, connectionTimeout,
                                  connectionAvailable);

        return client;
    }
    
    private static int getAsInt(String prop, int defaultValue) {
        
        if (prop == null) {
            return defaultValue;
        }
        try {
            int val = Integer.parseInt(prop);
            return val;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getMaxPermits()
    {
        return maxPermits;
    }
}
