package example.akismet;

/**
 * MBean for an {@link AkismetClientFactory}.
 */
public interface AkismetClientFactoryMBean {

    /**
     * Return the time to wait at most for an Akismet HTTP request.
     * 
     * @return timeout value in millisec.
     */
    int getSocketTimeout();

    /**
     * Set the time to wait at most for an Akismet HTTP request.
     * 
     * @param timeout
     *            timeout value in millisec.
     */
    void setSocketTimeout(int timeout);

    /**
     * Return the time to wait at most for an Akismet HTTP request.
     * 
     * @return timeout value in millisec.
     */
    int getConnectionTimeout();

    /**
     * Set the time to wait at most for establishing an Akismet HTTP request.
     * 
     * @param timeout
     *            timeout value in millisec.
     */
    void setConnectionTimeout(int timeout);
    
    /**
     * Return if the akismet service is enabled or not in this
     * installation.
     *
     * @return true if the service is enabled
     */
    boolean getAkismetEnabled();
    
    /**
     * Get the max concurrent request threads to allow.
     */
    long getMaxAllowedThread();

    /**
     * Return the current number of concurrent requesting threads. Returns an
     * accurate, but possibly unstable value, that may change immediately after
     * returning.
     */
    long getThreads();
}
