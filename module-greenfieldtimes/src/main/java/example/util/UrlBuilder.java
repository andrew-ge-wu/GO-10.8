package example.util;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.render.RenderRequest;


public class UrlBuilder {

    public String buildUrl(List<ContentId> path, ContentId stopId, RenderRequest request)
        throws CMException
    {

        int lastIndexOfStopId = path.lastIndexOf(stopId);
        if (lastIndexOfStopId > -1) {
        
            path = path.subList(0, lastIndexOfStopId + 1);
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            URLBuilder urlBuilder =
                RequestPreparator.getURLBuilder(httpRequest);
            
            ContentId[] pathAsArray = new ContentId[0];
            pathAsArray = path.toArray(pathAsArray);
            return urlBuilder.createUrl(pathAsArray, httpRequest);
        } else {
           throw new IllegalArgumentException("Path +" + path + " does not contain stopid" + stopId);
        }
        
    }

}
