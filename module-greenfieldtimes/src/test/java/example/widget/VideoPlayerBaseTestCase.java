package example.widget;

import org.mockito.Mock;

import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OSubmitButton;

import example.MockitoBase;

public abstract class VideoPlayerBaseTestCase
     extends MockitoBase
{
    @Mock protected OrchidContext oc;
    @Mock protected Policy policy;
    @Mock protected LinkResolver linkResolver;
    @Mock protected OSubmitButton submitButtonWidget;

    protected String videoLink = "http://videolink";;
    protected String imageLink = "http://imagelink";;

    public VideoPlayerBaseTestCase()
    {
        super();
    }

    public VideoPlayerBaseTestCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

    }
}