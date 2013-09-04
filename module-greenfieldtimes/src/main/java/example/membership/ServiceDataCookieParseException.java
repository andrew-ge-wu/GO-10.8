package example.membership;

public class ServiceDataCookieParseException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ServiceDataCookieParseException()
    {
        super();
    }

    public ServiceDataCookieParseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceDataCookieParseException(String message)
    {
        super(message);
    }

    public ServiceDataCookieParseException(Throwable cause)
    {
        super(cause);
    }
}
