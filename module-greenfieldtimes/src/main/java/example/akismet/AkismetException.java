package example.akismet;

/**
 * Thrown when a Akismet call fails (for example when a HTTP connection fails).
 */
public class AkismetException extends Exception {

    private static final long serialVersionUID = 8190329277581764723L;

    public AkismetException(String message) {
        super(message);
    }
    
    public AkismetException(String message, Throwable t) {
        super(message, t);
    }
}
