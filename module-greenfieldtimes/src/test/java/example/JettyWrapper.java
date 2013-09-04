/**
 * 
 */
package example;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;

public class JettyWrapper {

    int port;

    Server server;

    Context root;

    SocketConnector connector;

    public JettyWrapper() throws Exception {
        server = new Server();
        connector = new SocketConnector();
        server.addConnector(connector);
        root = new Context(server, "/", Context.SESSIONS);

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { root, new DefaultHandler() });

        server.setHandler(handlers);
    }

    /**
     * Get the port used, or -1 if server isn't started yet.
     */
    public int getPort() {
        return port;
    }

    public Context getContext() {
        return root;
    }
    
    public void addFilter(Filter filter, String mapping, String[] parameters)
    {
        FilterHolder filterHolder = new FilterHolder(filter);
        for (int i = 0; i < parameters.length; i+=2) {
            filterHolder.setInitParameter(parameters[i], parameters[i+1]);
        }
        root.addFilter(filterHolder, mapping, Handler.DEFAULT);
    }
    
    public void addFilter(Filter filter, String mapping)
    {
        addFilter(filter, mapping, new String[0]);
    }
    
    public void addServlet(Servlet servlet, String mapping, String[] parameters)
        throws Exception {
        ServletHolder servletHolder = new ServletHolder(servlet);
        for (int i = 0; i < parameters.length; i+=2) {
            servletHolder.setInitParameter(parameters[i], parameters[i+1]);
        }
        root.addServlet(servletHolder, mapping);
    }

    public void addServlet(Servlet servlet, String mapping)
            throws Exception {
        addServlet(servlet, mapping, new String[]{});
    }

    public void start() throws Exception {
        server.start();

        port = connector.getLocalPort();
    }
    
    public String getURL(String path) {
        return "http://localhost:" + port + path;
    }

    public void stop() throws Exception {
        server.stop();
    }
}