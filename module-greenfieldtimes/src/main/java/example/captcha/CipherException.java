package example.captcha;

/**
 * This exception is general for cipher encrypt and decrypt.
 */
public class CipherException extends Exception {

    private static final long serialVersionUID = 409080890824977072L;

    public CipherException(String message) {
        super(message);
    }
    
    public CipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
