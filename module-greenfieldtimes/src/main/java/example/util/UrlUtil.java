package example.util;

/**
 * Util for manipulating URLs.
 */
public class UrlUtil
{

    /**
     * Removes all request parameters with the name paramName.
     */
    public String stripRequestParam(String url, String paramName)
    {
        int paramStartIndex = url.indexOf('?');
        if (-1 == paramStartIndex) {
            return url;
        }
        String base = url.substring(0, paramStartIndex);
        String params = url.substring(paramStartIndex + 1);
    
        StringBuilder result = new StringBuilder(base);
        result.append("?");
        for (String param : params.split("&")) {
            if ((param.startsWith(paramName)
                 && (param.length() == paramName.length()
                     || '=' == param.charAt(paramName.length())))
                || 0 == param.length()) {
                continue;
            }
            result.append(param);
            result.append("&");
        }
        return result.substring(0, result.length() - 1);
    }
    
    public String appendQueryParam(String url, String key, String value)
    {
        StringBuilder sb = new StringBuilder(url);

        if (url.indexOf('?') >= 0) {
            sb.append('&');
        } else {
            sb.append('?');
        }

        sb.append(key);
        sb.append('=');
        sb.append(value);

        return sb.toString();
    }

}
