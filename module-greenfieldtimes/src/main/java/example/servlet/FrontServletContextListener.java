package example.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.polopoly.application.Application;
import com.polopoly.application.ApplicationStatusReporter;
import com.polopoly.application.ConnectionProperties;
import com.polopoly.application.ConnectionPropertiesException;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.LegacyDaemonThreadsStopper;
import com.polopoly.application.StandardApplication;
import com.polopoly.application.config.ConfigurationRuntimeException;
import com.polopoly.application.config.ResourceConfig;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.client.DiskCacheSettings;
import com.polopoly.cm.client.HttpCmClientHelper;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.poll.client.PollClient;
import com.polopoly.search.solr.SolrIndexName;
import com.polopoly.search.solr.SolrSearchClient;
import com.polopoly.statistics.client.StatisticsClient;
import com.polopoly.statistics.message.logging.UDPLogMsgClient;

import example.captcha.CaptchaSettingsPolicy;
import example.captcha.ClusteredImageCaptchaService;
import example.captcha.CookieImageCaptchaService;
import example.captcha.DefaultCaptchaFactory;

/**
 * Creates, inits and destroys the Application used in the front
 * webapp. The Application is also inserted in the ServletContext
 * under the name specified in web.xml.
 */
public class FrontServletContextListener
    implements ServletContextListener
{
    private static Logger LOG = Logger.getLogger(FrontServletContextListener.class.getName());

    private Application _application;

    public void contextInitialized(ServletContextEvent sce)
    {
        try {
            ServletContext sc = sce.getServletContext();

            // Application with local JMX registry.
            _application = new StandardApplication(ApplicationServletUtil.getApplicationName(sc));
            _application.setManagedBeanRegistry(ApplicationServletUtil.getManagedBeanRegistry());

            // CM Client.
            ConnectionProperties connectionProperties = ApplicationServletUtil.getConnectionProperties(sc);
            CmClientBase cmClient = HttpCmClientHelper.createAndAddToApplication(_application, connectionProperties);

            // Reports status back to the cm server
            _application.addApplicationComponent(new ApplicationStatusReporter(cmClient));

            // NOTE: The CmClient needs a heartbeat to get updates from the change
            // list and for reconnecting to the server.
            // In a web application the heartbeat is triggered by the
            // ApplicationHeartbeatFilter mapped under the name 'pacemaker'
            // in GreenfieldTimes' web.mxl.
            // For a standalone CmClient see example in the com.polopoly.application
            // package javadoc.

            // Solr search client (Searches in the public index).
            SolrSearchClient solrSearchClient =
                new SolrSearchClient(SolrSearchClient.DEFAULT_MODULE_NAME,
                                     SolrSearchClient.DEFAULT_COMPONENT_NAME,
                                     cmClient);
            solrSearchClient.setIndexName(new SolrIndexName("public"));

            _application.addApplicationComponent(solrSearchClient);

            // Statistics client.
            StatisticsClient statisticsClient = new StatisticsClient();
            _application.addApplicationComponent(statisticsClient);

            // Poll client.
            PollClient pollClient = new PollClient();
            _application.addApplicationComponent(pollClient);

            // Log msg client client.
            UDPLogMsgClient logMsgClient = new UDPLogMsgClient();
            _application.addApplicationComponent(logMsgClient);

            // Sync cache.
            LRUSynchronizedUpdateCache syncCache = new LRUSynchronizedUpdateCache();
            _application.addApplicationComponent(syncCache);

            // Read connection properties.
            _application.readConnectionProperties(connectionProperties);

            // Read and apply config in xml resources. Since this
            // webapp uses the same source for preview and front we
            // use the application name to distinguish between preview
            // and front config.
            ResourceConfig config = new ResourceConfig(_application.getName());
            config.apply(_application);

            // Configure disk cache settings
            DiskCacheSettings settings = cmClient.getDiskCacheSettings();
            ApplicationServletUtil.configureDiskCacheBaseDir(sce.getServletContext(), settings, _application.getName(), cmClient.getModuleName());
            cmClient.setDiskCacheSettings(settings);

            // Init.
            _application.init();

            // Put in global scope.
            ApplicationServletUtil.setApplication(sc, _application);
        }
        catch (IllegalApplicationStateException e) {
            throw new RuntimeException("This is a programming error, should never happend.", e);
        }
        catch (ConnectionPropertiesException e) {
            LOG.log(Level.SEVERE, "Could not get, read or apply connection properties.", e);
        }
        catch (ConfigurationRuntimeException e) {
            LOG.log(Level.SEVERE, "Could not read or apply configuration.", e);
        }

        try {
            setupCaptchaService(sce.getServletContext());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Could not start Captcha Service", e);
        }
    }

    private void setupCaptchaService(ServletContext sc)
    {
        if (null != _application) {
            ClusteredImageCaptchaService captchaService = null;

            CmClientBase client = (CmClientBase)
                _application.getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);

            PolicyCMServer policyCMServer = client.getPolicyCMServer();

            captchaService = new CookieImageCaptchaService(new DefaultCaptchaFactory(), policyCMServer);
            sc.setAttribute(CaptchaSettingsPolicy.CAPTCHA_SERVICE_SERVLET_CONTEXT_KEY, captchaService);
        }
    }

    public void contextDestroyed(ServletContextEvent sce)
    {
        ServletContext sc = sce.getServletContext();

        // Remove from global scope.
        ApplicationServletUtil.setApplication(sc, null);

        // Destroy.
        _application.destroy();

        LegacyDaemonThreadsStopper.stopStaticDaemons();
    }
}
