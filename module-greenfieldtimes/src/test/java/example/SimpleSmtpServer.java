package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dumbster.smtp.SmtpActionType;
import com.dumbster.smtp.SmtpMessage;
import com.dumbster.smtp.SmtpRequest;
import com.dumbster.smtp.SmtpResponse;
import com.dumbster.smtp.SmtpState;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SimpleSmtpServer implements Runnable {
    
    private static final Logger LOG = Logger.getLogger(SimpleSmtpServer.class.getName());
    
    /**
     * Stores all of the email received since this instance started up.
     */
    private List<SmtpMessage> receivedMail;

    /**
     * Default SMTP port is 25.
     */
    public static final int DEFAULT_SMTP_PORT = 25;

    /**
     * Indicates whether this server is stopped or not.
     */
    private volatile boolean stopped = true;

    /**
     * Handle to the server socket this server listens to.
     */
    private ServerSocket serverSocket;

    /**
     * Constructor.
     * 
     * @param port
     *            port number, if set to 0 then first available port will be
     *            used
     * @see #getPort()
     */
    public SimpleSmtpServer(ServerSocket serverSocket) {
        this.receivedMail = new ArrayList<SmtpMessage>();
        this.serverSocket = serverSocket;
    }

    /**
     * Main loop of the SMTP server.
     */
    public void run() {
        LOG.log(Level.INFO, "Starting dumbster SMTP server on "
                + serverSocket.getInetAddress().getHostAddress() + ":"
                + serverSocket.getLocalPort());
        stopped = false;
        try {
            // Server: loop until stopped
            while (!isStopped()) {
                // Start server socket and listen for client connections
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    if (socket != null) {
                        socket.close();
                    }
                    continue;
                    // Non-blocking socket timeout occurred,
                    // try accept() again
                }

                // Get the input and output streams received
                LOG.log(Level.INFO, "Got SMTP request from "
                        + socket.getInetAddress().getHostAddress() + ":"
                        + socket.getPort());
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                synchronized (this) {
                    /*
                     * We synchronize over the handle method and the list update
                     * because the client call completes inside the handle
                     * method and we have to prevent the client from reading the
                     * list until we've updated it. For higher concurrency, we
                     * could just change handle to return void and update the
                     * list inside the method to limit the duration that we hold
                     * the lock.
                     */
                    List<SmtpMessage> msgs = handleTransaction(out, input);
                    receivedMail.addAll(msgs);
                }
                socket.close();
            }
            LOG.log(Level.INFO, "Stopping dumbster SMTP server");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failur occured while listen for SMTP mails", e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check if the server has been placed in a stopped state. Allows another
     * thread to stop the server safely.
     * 
     * @return true if the server has been sent a stop signal, false otherwise
     */
    public synchronized boolean isStopped() {
        return stopped;
    }

    /**
     * Creates an instance of SimpleSmtpServer and starts it.
     * 
     * @param port
     *            port number the server should listen to, if set to 0 then
     *            first available port will be used
     * @return a reference to the SMTP server
     * @see #getPort()
     */
    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Stops the server. Server is shutdown after processing of the current
     * request is complete.
     */
    public synchronized void stop() {
        // Mark us closed
        stopped = true;
        try {
            // Kick the server accept loop
            serverSocket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Handle an SMTP transaction, i.e. all activity between initial connect and
     * QUIT command.
     * 
     * @param out
     *            output stream
     * @param input
     *            input stream
     * @return List of SmtpMessage
     * @throws IOException
     */
    private List<SmtpMessage> handleTransaction(PrintWriter out,
            BufferedReader input) throws IOException {
        // Initialize the state machine
        SmtpState smtpState = SmtpState.CONNECT;
        SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "",
                smtpState);

        // Execute the connection request
        SmtpResponse smtpResponse = smtpRequest.execute();

        // Send initial response
        sendResponse(out, smtpResponse);
        smtpState = smtpResponse.getNextState();

        List<SmtpMessage> msgList = new ArrayList<SmtpMessage>();
        SmtpMessage msg = new SmtpMessage();

        while (smtpState != SmtpState.CONNECT) {
            String line = input.readLine();

            if (line == null) {
                break;
            }

            // Create request from client input and current state
            SmtpRequest request = SmtpRequest.createRequest(line, smtpState);
            // Execute request and create response object
            SmtpResponse response = request.execute();
            // Move to next internal state
            smtpState = response.getNextState();
            // Send response to client
            sendResponse(out, response);

            // Store input in message
            String params = request.getParams();
            msg.store(response, params);

            // If message reception is complete save it
            if (smtpState == SmtpState.QUIT) {
                msgList.add(msg);
                msg = new SmtpMessage();
            }
        }

        return msgList;
    }

    /**
     * Send response to client.
     * 
     * @param out
     *            socket output stream
     * @param smtpResponse
     *            response object
     */
    private static void sendResponse(PrintWriter out, SmtpResponse smtpResponse) {
        if (smtpResponse.getCode() > 0) {
            int code = smtpResponse.getCode();
            String message = smtpResponse.getMessage();
            out.print(code + " " + message + "\r\n");
            out.flush();
        }
    }

    /**
     * Get email received by this instance since start up.
     * 
     * @return List of String
     */
    public synchronized Iterable<SmtpMessage> getReceivedEmail() {
        return receivedMail;
    }

    /**
     * Get the number of messages received.
     * 
     * @return size of received email list
     */
    public synchronized int getReceivedEmailSize() {
        return receivedMail.size();
    }
}
