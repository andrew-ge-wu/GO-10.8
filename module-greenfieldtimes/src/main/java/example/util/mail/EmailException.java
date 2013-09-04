package example.util.mail;

public class EmailException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>EmailException</code> with no specific message.
     */
    public EmailException()
    {
        super();
    }

    /**
     * Constructs a new <code>EmailException</code> with specified detail
     * message.
     *
     * @param msg the error message.
     */
    public EmailException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new <code>EmailException</code> with specified nested
     * <code>Throwable</code> root cause.
     *
     * @param rootCause  the exception or error that caused this exception
     *                   to be thrown.
     */
    public EmailException(Throwable rootCause)
    {
        super(rootCause);
    }

    /**
     * Constructs a new <code>EmailException</code> with specified detail
     * message and nested <code>Throwable</code> root cause.
     *
     * @param msg  the error message.
     * @param rootCause  the exception or error that caused this exception
     *                   to be thrown.
     */
    public EmailException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
