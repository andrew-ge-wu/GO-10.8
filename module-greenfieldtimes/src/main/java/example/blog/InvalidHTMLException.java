package example.blog;

public class InvalidHTMLException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidHTMLException(Throwable cause) {
        super(cause);
    }
}
