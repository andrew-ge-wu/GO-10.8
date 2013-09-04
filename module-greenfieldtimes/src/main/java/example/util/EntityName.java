package example.util;

import java.util.List;

import com.polopoly.metadata.Entity;

public class EntityName {
    public static String getEntityName(List<Entity> entityPath) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Entity entity : entityPath) {
            if (first) {
                first = false;
            } else {
                sb.append("/");
            }
            sb.append(entity.getName());
        }
        return sb.toString();
    }
}
