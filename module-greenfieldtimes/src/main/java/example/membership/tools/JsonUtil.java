package example.membership.tools;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class used to parse JSON strings to
 * actual JSON objects.
 */
public class JsonUtil
{
    private final Gson parser = new Gson();

    /**
     * Parse the given JSON string to a JSON object.
     * 
     * @param jsonString the JSON string to parse
     * @return the parsed JSON object
     * 
     * @throws JsonSyntaxException if the JSON string is invalid
     */
    public JsonElement toJson(String jsonString)
        throws JsonSyntaxException
    {
        return parser.fromJson(jsonString, JsonElement.class);
    }

    public String toString(JsonElement json) {
        return parser.toJson(json);
    }
}
