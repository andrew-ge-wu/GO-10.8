package example.widget;

import java.io.IOException;

@SuppressWarnings("serial")
public class UnresolvableLinkException
    extends Exception
{

    public UnresolvableLinkException(String string)
    {
        super(string);
    }

    public UnresolvableLinkException(String string, IOException e)
    {
        super(string, e);
    }

}
