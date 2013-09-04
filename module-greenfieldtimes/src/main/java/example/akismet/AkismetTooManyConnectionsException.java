package example.akismet;

/**
 * Thrown when too many Akismet clients try to access the Akismet
 * servers at the same time.
 */
public class AkismetTooManyConnectionsException extends Exception {

    private static final long serialVersionUID = 3130713282016986031L;

    public AkismetTooManyConnectionsException() {
        super();
    }
    
    public AkismetTooManyConnectionsException(String message) {
        super(message);
    }

    public AkismetTooManyConnectionsException(String message, Throwable t) {
        super(message, t);
    }

}
