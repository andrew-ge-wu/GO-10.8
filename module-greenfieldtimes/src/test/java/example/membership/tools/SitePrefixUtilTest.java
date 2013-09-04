package example.membership.tools;

import junit.framework.TestCase;

public class SitePrefixUtilTest extends TestCase
{
    private SitePrefixUtil _toTest;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _toTest = new SitePrefixUtil();
    }
    
    public void testStripNull() throws Exception
    {
        assertNull(_toTest.stripPrefix(null));
    }
    
    public void testStripEmpty() throws Exception
    {        
        assertNull(_toTest.stripPrefix(""));
    }
    
    public void testStripCorrect() throws Exception
    {
        String siteId = "2.158";
        String loginName = "adrian@greenfieldtimes.com";
        
        String prefixedLoginName = _toTest.addPrefix(siteId, loginName);
        
        assertEquals(loginName, _toTest.stripPrefix(prefixedLoginName));        
    }
    
    public void testStripMultipleSeparators() throws Exception
    {
        String siteId = "2.158";
        String loginName = "adrian"
                           + SitePrefixUtil.SEPARATOR
                           + "smith@greenfieldtimes.com";
        
        String prefixedLoginName = _toTest.addPrefix(siteId, loginName);
        
        assertEquals(loginName, _toTest.stripPrefix(prefixedLoginName));        
    }
    
    public void testStripNullLoginName() throws Exception
    {
        String siteId = "2.158";
        String loginName = null;
        
        String prefixedLoginName = _toTest.addPrefix(siteId, loginName);
        
        assertNull(_toTest.stripPrefix(prefixedLoginName));
    }
    
    public void testStripEmptyLoginName() throws Exception
    {
        String siteId = "2.158";
        String loginName = "";
        
        String prefixedLoginName = _toTest.addPrefix(siteId, loginName);
        
        assertNull(_toTest.stripPrefix(prefixedLoginName));
    }
    
    public void testStripShortLoginName() throws Exception
    {
        String siteId = "2.158";
        String loginName = "a";
        
        String prefixedLoginName = _toTest.addPrefix(siteId, loginName);
        
        assertEquals(loginName, _toTest.stripPrefix(prefixedLoginName));        
    }
}
