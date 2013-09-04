package example;

import junit.framework.TestCase;

import org.mockito.MockitoAnnotations;

public class MockitoBase extends TestCase {
    
    public MockitoBase()
    {
        super();
    }

    public MockitoBase(String arg0)
    {
        super(arg0);
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

}
