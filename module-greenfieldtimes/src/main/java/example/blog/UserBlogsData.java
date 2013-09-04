package example.blog;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import example.membership.CookieBackedServiceData;

/**
 * User blogs bean class.
 */
public class UserBlogsData implements CookieBackedServiceData
{
    private List<String> blogs;

    public UserBlogsData()
    {
        this.blogs = new ArrayList<String>();
    }
    
    public List<String> getBlogs()
    {
        return blogs;
    }

    public void setBlogs(List<String> blogs)
    {
        this.blogs = blogs;
    }
 
    public JsonArray loadToJsonCookie()
    {
        JsonArray jsonArray = new JsonArray();
        for (String blog : blogs) {
            jsonArray.add(new JsonPrimitive(blog));
        }
        return jsonArray;
    }

    public void storeFromJsonCookie(JsonElement jsonData)
    {
        // Not implemented
    }
}
