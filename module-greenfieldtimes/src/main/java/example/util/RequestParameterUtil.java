package example.util;

import com.polopoly.render.RenderRequest;

public class RequestParameterUtil {
    public static final String PARAMETER_AJAX = "ajax";

    public int getInt(RenderRequest request,
                      String key,
                      int defaultValue,
                      int minValue,
                      int maxValue)
    {
        int value = defaultValue;
        try {
            String parameter = request.getParameter(key);
            if (parameter != null) {
                value = Integer.parseInt(parameter);
                if (value > maxValue) {
                    value = maxValue;
                } else if (value < minValue) {
                    value = minValue;
                }
            }
        } catch (NumberFormatException keepDefault) {
        }
        return value;
    }

    public boolean isAjaxRequestMode(RenderRequest request)
    {
        return null != request.getParameter(PARAMETER_AJAX);
    }
}
