package com.cs14.ytsupportagent.processor;

import com.cs14.ytsupportagent.service.AgentNotificationService;
import com.cs14.ytsupportagent.service.CentralizedStreamingAgentService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("centralizedStreamingAgentProcessor")
public class CentralizedStreamingAgentProcessor implements Processor {

    private final AgentNotificationService notificationService;
    private final CentralizedStreamingAgentService streamingAgentService;

    public CentralizedStreamingAgentProcessor(AgentNotificationService notificationService, CentralizedStreamingAgentService streamingAgentService) {
        this.notificationService = notificationService;
        this.streamingAgentService = streamingAgentService;
    }


    @Override
    public void process(Exchange exchange) throws Exception {
        String clientId = exchange.getProperty("ClientId", String.class);
        String userMessage = exchange.getIn().getBody(String.class);
        String systemPrompt = exchange.getProperty("AgentSystemPrompt", String.class);
        String agentName = exchange.getProperty("AgentName","Agent", String.class);
        notificationService.sendStatus(clientId, "STATUS", "["+agentName+"] Started processing your request");

        streamingAgentService.chat(clientId, systemPrompt, userMessage)
                .onPartialResponse(token -> notificationService.sendStatus(clientId, "TOKEN", escapeJson(token)))
                .onCompleteResponse(response-> notificationService.sendStatus(clientId, "STEAM_END", ""))
                .onError(error -> notificationService.sendStatus(clientId, "ERROR", escapeJson(error.getMessage())))
                .start();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
