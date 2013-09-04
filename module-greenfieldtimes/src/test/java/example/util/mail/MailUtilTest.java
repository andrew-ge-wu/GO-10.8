package example.util.mail;

import junit.framework.TestCase;

/**
 * Unit test for MailUtil.
 */
public class MailUtilTest extends TestCase
{
    public void testAlmostEmail()
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("a@b"));
    }
    
    public void testNotEmailAtAll() 
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("Gunnar Relin"));
    }
    
    public void testValidEmail() 
        throws Exception
    {
        assertTrue(MailUtil.isValidEmailAddress("a@a.dk"));
    }
    
    public void testCrazyMultilingualEmail() 
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("s\u00f6ren.br\u00e4dg\u00e5rd@h\u00f6\u00f6r.se"));
    }
    
    public void testAnotherValidEmail() 
        throws Exception
    {
        assertTrue(MailUtil.isValidEmailAddress("marcus.frodin+min-adress@a.polopoly.co.uk"));
    }
    
    public void testAlotOfAsciiInEmail() 
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("++++++++@foo.baz.com"));
    }
   
    public void testMultipleAtInEmail() 
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("urban@@polopoly.com"));
    }
    
    public void testCrossSiteScripting()
        throws Exception
    {
        assertFalse(MailUtil.isValidEmailAddress("%27%3B%61%6C%65%72%74%28%53%74%72%69%6E%67%2E%66%72%6F%6D%43%68%61%72%43%6F%64%65%28%38%38%2C%38%33%2C%38%33%29%29%2F%2F%5C%27%3B%61%6C%65%72%74%28%53%74%72%69%6E%67%2E%66%72%6F%6D%43%68%61%72%43%6F%64%65%28%38%38%2C%38%33%2C%38%33%29%29%2F%2F%22%3B%61%6C%65%72%74%28%53%74%72%69%6E%67%2E%66%72%6F%6D%43%68%61%72%43%6F%64%65%28%38%38%2C%38%33%2C%38%33%29%29%2F%2F%5C%22%3B%61%6C%65%72%74%28%53%74%72%69%6E%67%2E%66%72%6F%6D%43%68%61%72%43%6F%64%65%28%38%38%2C%38%33%2C%38%33%29%29%2F%2F%2D%2D%3E%3C%2F%53%43%52%49%50%54%3E%22%3E%27%3E%3C%53%43%52%49%50%54%3E%61%6C%65%72%74%28%53%74%72%69%6E%67%2E%66%72%6F%6D%43%68%61%72%43%6F%64%65%28%38%38%2C%38%33%2C%38%33%29%29%3C%2F%53%43%52%49%50%54%3E@polopoly.com"));
        assertFalse(MailUtil.isValidEmailAddress("%22%20%6F%6E%63%6C%69%63%6B%3D%22%6A%61%76%61%73%63%72%69%70%74%3A%20%61%6C%65%72%74%28%27%68%65%6A%27%29%22@polopoly.com"));
    }
}
