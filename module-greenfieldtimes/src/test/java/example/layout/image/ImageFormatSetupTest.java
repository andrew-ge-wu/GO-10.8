package example.layout.image;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.polopoly.cm.app.imagemanager.ImageSet;

import example.MockitoBase;

public class ImageFormatSetupTest
    extends MockitoBase
{
    String[] derivatives = new String[] {
        "landscape_804",
        "landscape_490",
        "box_80",
        "box_58",
        "landscape_300",
        "landscape_240",
        "box_160",
        "box_100",
        "landscape_174",
    };

    String[] heightDerivates = new String[] {
        "landscape_804",
        "landscape408_804",
        "landscape408_400",
        "landscape_300",
        "box_100",
        "box30_50"
    };

    ImageFormatSetup target;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        target = new ImageFormatSetup();
    }

    public void testShouldGetMatchingDerivativeName()
        throws Exception
    {
        ImageSet imageSet = mock(ImageSet.class);
        when(imageSet.getDerivativeNames()).thenReturn(derivatives);

        assertEquals(160, target.getMatchingDerivativeDimension("box", target.dim(240, -1), imageSet).width);
        assertEquals(0, target.getMatchingDerivativeDimension("box", target.dim(40, -1), imageSet).width);
        assertEquals(58, target.getMatchingDerivativeDimension("box", target.dim(75, -1), imageSet).width);
        assertEquals(100, target.getMatchingDerivativeDimension("box", target.dim(159, -1), imageSet).width);

        assertEquals(174, target.getMatchingDerivativeDimension("landscape", target.dim(239, -1), imageSet).width);
        assertEquals(490, target.getMatchingDerivativeDimension("landscape", target.dim(800, -1), imageSet).width);
        assertEquals(804, target.getMatchingDerivativeDimension("landscape", target.dim(1024, -1), imageSet).width);
        assertEquals(300, target.getMatchingDerivativeDimension("landscape", target.dim(400, -1), imageSet).width);
    }

    public void testHeightRestrictedDerivateNames()
        throws Exception
    {
        ImageSet imageSet = mock(ImageSet.class);
        when(imageSet.getDerivativeNames()).thenReturn(heightDerivates);

        assertEquals(target.dim(804, -1), target.getMatchingDerivativeDimension("landscape", target.dim(804, -1), imageSet));
        assertEquals(target.dim(804, -1), target.getMatchingDerivativeDimension("landscape", target.dim(900, -1), imageSet));
        assertEquals(target.dim(804, 408), target.getMatchingDerivativeDimension("landscape", target.dim(804, 408), imageSet));
        assertEquals(target.dim(804, 408), target.getMatchingDerivativeDimension("landscape", target.dim(900, 500), imageSet));
        assertEquals(target.dim(100, -1), target.getMatchingDerivativeDimension("box", target.dim(100, -1), imageSet));
        assertEquals(target.dim(50, 30), target.getMatchingDerivativeDimension("box", target.dim(100, 50), imageSet));
        assertEquals(target.dim(300, -1), target.getMatchingDerivativeDimension("landscape", target.dim(400, 100), imageSet));
    }

    public void testShouldGetMaxImageWidth()
        throws Exception
    {
        assertEquals(target.dim(400, -1), target.getMaxImageSize("landscape", target.dim(400, -1)));
        assertEquals(target.dim(100, -1), target.getMaxImageSize("box", target.dim(300, -1)));
    }
}
