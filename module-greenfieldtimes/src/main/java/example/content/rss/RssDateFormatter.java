package example.content.rss;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import example.util.ThreadLocalDateFormat;

public class RssDateFormatter
{
    private final ThreadLocalDateFormat threadLocalDateFormat;

    public RssDateFormatter()
    {
        threadLocalDateFormat =
            new ThreadLocalDateFormat("E, d MMM yyyy HH:mm:ss Z",
                                      TimeZone.getTimeZone("UTC Universal"),
                                      Locale.ENGLISH);
    }

    public String format(Date date) {
        return threadLocalDateFormat.format(date);
    }
}
