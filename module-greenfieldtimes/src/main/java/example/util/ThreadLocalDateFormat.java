package example.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ThreadLocalDateFormat
    extends DisposableThreadLocal<SimpleDateFormat>
{
    private final SimpleDateFormat originalFormat;

    public ThreadLocalDateFormat(final String pattern,
                                 final TimeZone timeZone,
                                 final Locale locale)
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        format.setTimeZone(timeZone);

        originalFormat = format;
    }

    @Override
    public SimpleDateFormat get()
    {
        SimpleDateFormat format = super.get();

        if (format == null) {
            format = (SimpleDateFormat) originalFormat.clone();
            super.set(format);
        }

        return format;
    }

    public Date parse(String source)
        throws ParseException
    {
        return get().parse(source);
    }

    public String format(long time)
    {
        return get().format(new Date(time));
    }

    public String format(Date date)
    {
        return get().format(date);
    }
}
