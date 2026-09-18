package com.cs14.ytsupportagent.processor;


import com.cs14.ytsupportagent.service.AgentNotificationService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("webSocketFrameProcessor")
public class WebSocketFrameProcessor implements Processor {
    private final AgentNotificationService notificationService;

    public WebSocketFrameProcessor(AgentNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String clientId = exchange.getProperty("ClientId", String.class);
        String frameType = exchange.getProperty("FrameType", "STATUS", String.class);
        String frameMessage = exchange.getProperty("FrameMessage", "", String.class);

        if (!frameMessage.isBlank()) {
            notificationService.sendStatus(clientId,frameType,escapeJson(frameMessage));
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}