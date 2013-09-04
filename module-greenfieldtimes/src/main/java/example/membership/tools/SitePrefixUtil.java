package example.membership.tools;

public class SitePrefixUtil
{
    static final char SEPARATOR = '_';

    public String addPrefix(String siteId, String loginName) {
        return siteId != null && loginName != null
                   ? siteId + SEPARATOR + loginName
                   : null;
    }
    
    public String stripPrefix(String prefixedLoginName)
    {
        if (prefixedLoginName == null) {
            return null;
        }
        int sitePrefixEnds = prefixedLoginName.indexOf(SEPARATOR);
        if (sitePrefixEnds < 0 || sitePrefixEnds == prefixedLoginName.length() - 1) {
            return null;
        } else {
            return prefixedLoginName.substring(sitePrefixEnds + 1);
        }
    }
}
