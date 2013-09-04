package example.layout.image;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.app.imagemanager.ImageSet;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.siteengine.model.TopModel;

public class ImageFormatSetup {

    private static final Logger LOG = Logger.getLogger(ImageFormatSetup.class.getName());

    public String setupImageDerivativeKeyInModel(TopModel m, String derivativeType, ImageSet image)
    {
        Dimension imageSize = dim(0, -1);

        if (derivativeType == null) {
            derivativeType = "landscape";
        }

        try {
            Dimension columnSize = getColumnSize(m);
            Dimension maxImageSize = getMaxImageSize(derivativeType, columnSize);
            imageSize = getMatchingDerivativeDimension(derivativeType, maxImageSize, image);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "No column width provided. No image returned.", e);
        }

        ModelPathUtil.set(m.getLocal(), "imageWidth", imageSize.width);
        String imageDerivativeKey = null;
        if (imageSize.height> 0 && imageSize.width > 0) {
            imageDerivativeKey = derivativeType + imageSize.height + "_" + imageSize.width;
        } else if (imageSize.width > 0) {
            imageDerivativeKey = derivativeType + "_" + imageSize.width;
        }
        ModelPathUtil.set(m.getLocal(), "imageDerivativeKey", imageDerivativeKey);
        return imageDerivativeKey;
    }

    Dimension getMaxImageSize(String derivativeType, Dimension columnSize)
    {
        if ("box".equals(derivativeType)) {
            return dim(columnSize.width / 3, columnSize.height);
        }
        return columnSize;
    }

    private Dimension getColumnSize(TopModel m) {
        return dim(getAsInt(m, "colwidth"), getAsInt(m, "slotHeight"));
    }

    private int getAsInt(TopModel m, String attribute)
    {
        Object colWidthObj = m.getStack().getAttribute(attribute);
        int columnWidth = -1;
        if (colWidthObj instanceof String) {
            columnWidth  = Integer.parseInt((String) colWidthObj);
        } else if (colWidthObj instanceof Integer) {
            columnWidth = ((Integer) colWidthObj).intValue();
        } else if (colWidthObj instanceof Double) {
            columnWidth = ((Double) colWidthObj).intValue();
        }
        return columnWidth;
    }

    Dimension getMatchingDerivativeDimension(String derivativeType,
                                             Dimension column,
                                             ImageSet image)
    {
        Dimension current = dim(0, -1);
        Dimension fallback = dim(0, -1);
        for (String derivativeKey : image.getDerivativeNames()) {
            if (derivativeKey.startsWith(derivativeType)) {
                Dimension derivative = getDerivative(derivativeKey);

                boolean largerThanCurrent = derivative.width > current.width;
                boolean largerThanFallback = derivative.width > fallback.width;
                boolean widthIsOk = derivative.width <= column.width;
                boolean noHeight = derivative.height == -1;
                boolean heightIsOk = (column.height == -1 && derivative.height == -1) ||
                                     (column.height > 0 && derivative.height > 0 && derivative.height <= column.height);
                boolean matchesCurrent = derivative.width == current.width;
                boolean heightLargerThanCurrent = derivative.height > current.height;

                if (widthIsOk && heightIsOk && (largerThanCurrent || (matchesCurrent && heightLargerThanCurrent))) {
                    current = derivative;
                }
                if (widthIsOk && noHeight && largerThanFallback) {
                    fallback = derivative;
                }
            }
        }
        if (current.width > 0) {
            return current;
        }
        return fallback;
    }

    private Dimension getDerivative(String derivativeKey)
    {
        String[] keys = derivativeKey.split("_");
        int derivativeWidth = Integer.parseInt(keys[1]);
        String name = keys[0];
        int start = name.length() - 1;
        while (start >= 0 && Character.isDigit(name.charAt(start))) {
            start--;
        }
        if (start + 1 >= name.length()) {
            return dim(derivativeWidth, -1);
        }
        return dim(derivativeWidth, Integer.parseInt(name.substring(start + 1)));
    }

    Dimension dim(int w, int h) {
        return new Dimension(w, h);
    }

    static class Dimension {
        public final int width;
        public final int height;
        public Dimension(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
        @Override
        public String toString()
        {
            return "w" + width + "h" + height;
        }
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + height;
            result = prime * result + width;
            return result;
        }
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Dimension other = (Dimension) obj;
            if (height != other.height)
                return false;
            if (width != other.width)
                return false;
            return true;
        }
    }
}
