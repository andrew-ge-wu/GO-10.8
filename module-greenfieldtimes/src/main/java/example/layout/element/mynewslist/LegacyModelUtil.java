package example.layout.element.mynewslist;

import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;

public class LegacyModelUtil
{
    public Object get(Model model, String path)
    {
        return ModelPathUtil.get(model, path);
    }
}
