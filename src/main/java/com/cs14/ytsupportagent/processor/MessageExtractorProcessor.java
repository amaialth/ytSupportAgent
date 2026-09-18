package com.cs14.ytsupportagent.processor;


import com.cs14.ytsupportagent.agent.AgentRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("messageExtractorProcessor")
public class MessageExtractorProcessor implements Processor {

    @Autowired
    private AgentRegistry agentRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        String rawBody = exchange.getIn().getBody(String.class);
        String clientId = "DEFAULT_SESSION";
        String userText = rawBody;

        try {
            JsonNode node = objectMapper.readTree(rawBody);
            if (node.has("clientId")) clientId = node.get("clientId").asText();
            if (node.has("text")) userText = node.get("text").asText();
        } catch (Exception ignored) {}

        exchange.getIn().setBody(userText);
        exchange.setProperty("ClientId", clientId);

        String activeDomain = agentRegistry.getActiveDomain(clientId);
        exchange.setProperty("ActiveDomain", activeDomain);
    }
}
