package example.akismet;

import java.util.concurrent.Semaphore;

/**
 * MBean implementation for {@link AkismetClientFactoryMBean}.
 */
public class AkismetClientFactoryMBeanImpl implements AkismetClientFactoryMBean {

    private final AkismetClientFactory akismetFactory;

    private final Semaphore semaphore;

    public AkismetClientFactoryMBeanImpl(Semaphore semaphore,
                                         AkismetClientFactory akismetFactory) 
    {
        this.semaphore = semaphore;
        this.akismetFactory = akismetFactory;
    }

    public boolean getAkismetEnabled() {
        return akismetFactory.hasAPIKey();
    }
    
    public int getSocketTimeout() {
        return (akismetFactory != null) ? akismetFactory.getSocketTimeout()
                : -1;
    }
    
    public int getConnectionTimeout() {
        return (akismetFactory != null) ? akismetFactory.getConnectionTimeout()
                : -1;
    }

    public void setSocketTimeout(int timeout) {
        if (akismetFactory != null) {
            akismetFactory.setSocketTimeout(timeout);
        }
    }
    
    public void setConnectionTimeout(int timeout) {
        if (akismetFactory != null) {
            akismetFactory.setConnectionTimeout(timeout);
        }
    }

    public long getThreads() {
        long allowedThreads = getMaxAllowedThread();
        long permits = (semaphore != null) ? semaphore.availablePermits()
                                           : allowedThreads;
        return allowedThreads - permits;
    }

    public long getMaxAllowedThread()
    {
        return (akismetFactory != null) ? akismetFactory.getMaxPermits()
                : -1;        
    }
}
