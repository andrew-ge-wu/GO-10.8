package example.membership.mynewslist;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import example.membership.CookieBackedServiceData;
import example.membership.tools.JsonUtil;

/**
 * My news list bean class, one bean contains settings for all news lists on the site for
 * a single user. This bean contains all my news list settings for a single user. The
 * bean is stored and loaded from the user's content with the UserDataManager.
 * 
 * The bean methods on this class will be used when storing and loading it from content, so
 * only data will be stored in this case.
 */
public class MyNewsListData
    implements CookieBackedServiceData
{
    private String _data;
    
    private final JsonUtil jsonUtil = new JsonUtil();
   
    public String getData()
    {
        return _data;
    }

    public void setData(String data)
    {
        _data = data;
    }

    public JsonElement loadToJsonCookie()
        throws JsonSyntaxException
    {
        // Just load the String as is. All knowledge of the format exists only in Javascript.
        return jsonUtil.toJson(_data);
    }

    public void storeFromJsonCookie(JsonElement jsonData)
    {
        // Just store the String as is. All knowledge of the format exists only in Javascript.
        setData(jsonUtil.toString(jsonData));
    }
}
