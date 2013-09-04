package example;

import java.net.ServerSocket;

import com.dumbster.smtp.SmtpMessage;

public class DumbsterWrapper {

    int port = -1;

    ServerSocket connector;
    
    SimpleSmtpServer simpleSmtpServer;
    
    public DumbsterWrapper() throws Exception {
        connector = new ServerSocket(0);
        connector.setSoTimeout(1500);
        simpleSmtpServer = new SimpleSmtpServer(connector);
    }
    
    public void start() {
        simpleSmtpServer.start();
        port = connector.getLocalPort();
    }
    
    public void stop() {
        simpleSmtpServer.stop();
    }
    
    public int getPort() {
        return port;
    }

    public int getReceivedEmailSize() {
        if (simpleSmtpServer != null) {
            return simpleSmtpServer.getReceivedEmailSize();
        }
        return -1;
    }

    public Iterable<SmtpMessage> getReceivedEmail() {
        if (simpleSmtpServer != null) {
            return simpleSmtpServer.getReceivedEmail();
        }
        return null;
    }
}
