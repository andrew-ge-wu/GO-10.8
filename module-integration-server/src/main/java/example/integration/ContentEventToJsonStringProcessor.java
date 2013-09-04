package example.integration;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.EjbCmClient;
import com.polopoly.cm.event.ContentEvent;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.integration.IntegrationServerApplication;

/**
 * Processor that replaces the message body content event with a string
 * representation
 */
public class ContentEventToJsonStringProcessor implements Processor {
    private Gson gson = new Gson();

    public void process(Exchange exchange) throws Exception {
        CmClient cmClient = (CmClient) IntegrationServerApplication.getPolopolyApplication().getApplicationComponent(
                EjbCmClient.DEFAULT_COMPOUND_NAME);
        PolicyCMServer cmServer = cmClient.getPolicyCMServer();

        Object body = exchange.getIn().getBody();
        if (body instanceof ContentEvent) {
            ContentEvent contentEvent = (ContentEvent) body;
            ContentId contentId = contentEvent.getContentId();
            String name = cmServer.getPolicy(contentId).getContent().getName();
            JsonObject jsonMap = new JsonObject();
            jsonMap.addProperty("principalIdString", contentEvent.principalIdString);
            jsonMap.addProperty("eventType", contentEvent.eventType);
            jsonMap.addProperty("contentId", contentEvent.getContentId().getContentIdString());
            jsonMap.addProperty("name", name);
            exchange.getIn().setBody(gson.toJson(jsonMap) + "\n");
        } else {
            exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
        }
    }
}
