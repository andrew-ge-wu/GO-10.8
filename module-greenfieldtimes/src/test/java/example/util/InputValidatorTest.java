package example.util;

import junit.framework.TestCase;


public class InputValidatorTest
    extends TestCase
{
    
    private InputValidator validator;

    protected void setUp() throws Exception
    {
        this.validator = new InputValidator();
    }
    
    public void testValidScreenName()
        throws Exception
    {
        assertTrue(validator.isValidScreenName("urbanh"));
        assertTrue(validator.isValidScreenName("Urban Henriksson"));
        assertTrue(validator.isValidScreenName("\u00c5\u00c4\u00d6 \u00e5\u00e4\u00f6"));
    }
    
    public void testEmptyScreenName()    
        throws Exception
    {
        assertFalse(validator.isValidScreenName("   "));
        assertFalse(validator.isValidScreenName(""));
        assertFalse(validator.isValidScreenName(null));
    }
    
    public void testStrangeScreenName()
        throws Exception
    {
        assertFalse(validator.isValidScreenName("[lf] /{}etasson"));
        assertFalse(validator.isValidScreenName("YES!!!!!"));
        assertFalse(validator.isValidScreenName("<script language='js'>Hupp</script>"));
    }
    
    public void testTooLongScreenName()
        throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("ABC");
        }
        assertFalse(validator.isValidScreenName(sb.toString()));
    }
}
