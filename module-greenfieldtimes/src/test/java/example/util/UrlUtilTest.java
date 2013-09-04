package example.util;

import junit.framework.TestCase;

/**
 * Unit test for the ParentPathResolver util.
 */
public class UrlUtilTest
    extends TestCase
{

    private static String URL_BASE = "http://www.greenfieldtimes.com";
    
    private UrlUtil toTest;
    
    protected void setUp()
        throws Exception
    {
        toTest = new UrlUtil();
    }

    public void testSingleStrip()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE + "?param=foo", "param"));
    }

    public void testStrangeUrl1()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE + "?", "param"));
    }

    public void testStrangeUrl2()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE + "?&param=&", "param"));
    }

    public void testStrangeUrl3()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE + "?param====", "param"));
    }

    public void testStrangeUrl4()
        throws Exception
    {
        assertEquals(URL_BASE + "?=", toTest.stripRequestParam(URL_BASE + "?&=&", "param"));
    }
    
    public void testStripNoValue()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE + "?param", "param"));
    }

    public void testNoStripSubstringName()
        throws Exception
    {
        assertEquals(URL_BASE + "?param=val", toTest.stripRequestParam(URL_BASE + "?param=val", "p"));
    }
    
    public void testStripWhenParamDoesNotExist()
        throws Exception
    {
        assertEquals(URL_BASE + "?p1=1&p2=2", toTest.stripRequestParam(URL_BASE + "?p1=1&p2=2", "param"));
    }

    public void testStripMultiples()
        throws Exception
    {
        assertEquals(URL_BASE + "?p1=1&p3=3&p4=5",
                     toTest.stripRequestParam(URL_BASE + "?p1=1&p2=2&p3=3&p2=4&p4=5", "p2"));
    }
    
    public void testNoParam()
        throws Exception
    {
        assertEquals(URL_BASE, toTest.stripRequestParam(URL_BASE, "com"));
    }
    
    public void testAppendSingleParam() 
        throws Exception 
    {
        assertEquals(URL_BASE + "?a=b", toTest.appendQueryParam(URL_BASE, "a", "b"));
    }
    
    public void testAppendMultiParam() 
        throws Exception 
    {
        assertEquals(URL_BASE + "?a=b&b=c", toTest.appendQueryParam(URL_BASE + "?a=b", "b", "c"));
    }
    
}

