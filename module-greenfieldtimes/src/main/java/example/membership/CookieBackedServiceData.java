package example.membership;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Interface for service types that wish to have all or part of its persistent
 * data cached in a browser cookie in order to minimize server requests.
 */
public interface CookieBackedServiceData
{
    /**
     * Store the given JSON data to persistent storage (content). This method is called
     * when an AJAX post is done to the /membership/persist URL.
     * 
     * @param jsonData the data to persist
     */
    void storeFromJsonCookie(JsonElement jsonData);
    
    /**
     * Load the persistent JSON data from persistent storage (content). This method is
     * called when a user logs in using the /membership/login URL.
     * 
     * @return the persistent data
     * @throws JsonSyntaxException if the persisted data is not valid JSON data
     */
    JsonElement loadToJsonCookie() throws JsonSyntaxException;
}
