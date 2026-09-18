package com.cs14.ytsupportagent.processor;


import com.cs14.ytsupportagent.agent.AgentRegistry;
import com.cs14.ytsupportagent.agent.AgentStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("classificationDispatchProcessor")
public class ClassificationDispatchProcessor implements Processor {

    @Autowired
    private AgentRegistry agentRegistry;

    @Override
    public void process(Exchange exchange) throws Exception {
        String response = exchange.getIn().getBody(String.class);
        String domain = (response != null && response.contains("[BILLING]")) ? "BILLING" : "GENERAL";
        String clientId = exchange.getProperty("ClientId", String.class);

        // Lock session and fetch assigned strategy
        agentRegistry.lockSession(clientId, domain);
        AgentStrategy strategy = agentRegistry.getStrategy(domain);

        // Populate properties for downstream processors & sub-routes
        exchange.setProperty("AgentName", strategy.getAgentName());
        exchange.setProperty("AgentSystemPrompt", strategy.getSystemPrompt());

        // Populate WebSocket frame properties for handoff notification
        exchange.setProperty("FrameType", "HANDOFF");
        exchange.setProperty("FrameMessage", strategy.getHandoffMessage());
    }
}
