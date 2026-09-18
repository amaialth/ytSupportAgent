package com.cs14.ytsupportagent.service;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class AgentNotificationService {
    private final ProducerTemplate producerTemplate;


    public AgentNotificationService(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    /**
     * Sends a status or handoff frame to the frontend WebSocket via Camel's ProducerTemplate.
     */
    public void sendStatus(String clientId, String type, String message) {
        String jsonPayload = String.format(
                "{\"clientId\":\"%s\",\"type\":\"%s\",\"message\":\"%s\"}",
                clientId, type, escapeJson(message)
        );

        // Send to direct:sendWebSocketResponse sub-route asynchronously
        producerTemplate.sendBody("direct:sendWebSocketResponse", jsonPayload);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
