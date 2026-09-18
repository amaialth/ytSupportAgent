package com.cs14.ytsupportagent.route;

import com.cs14.ytsupportagent.agent.AgentRegistry;
import com.cs14.ytsupportagent.agent.AgentStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class AgentRoute extends RouteBuilder {
    private static final String WEBSOCKET_CONNECTION_KEY = "CamelVertxWebsocket.connectionKey";
    private final ConcurrentMap<String, String> clientConnections = new ConcurrentHashMap<>();
    private final AgentRegistry agentRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${agent.handoffs.classifier-status}")
    private String classifierStatusMessage;
    private static final String SYSTEM_PROMPT = """
        
        You are a specialized Customer Support Ticket Classifier.
        Analyze the incoming user message and classify it into EXACTLY ONE of the following categories:
        - [TECHNICAL_ISSUE]
        - [BILLING]
        - [FEATURE_REQUEST]
        - [GENERAL_QUERY]
        
        Respond ONLY in this JSON format:
        {"category": "<CATEGORY>", "confidence": <SCORE 0.0-1.0>, "reason": "<SHORT_REASON>"}
        """;
    Predicate isStatus = body().contains("\"type\":\"STATUS\"");
    Predicate isToken = body().contains("\"type\":\"TOKEN\"");
    Predicate isHandoff = body().contains("\"type\":\"HANDOFF\"");
    Predicate isStreamEnd = body().contains("\"type\":\"STREAM_END\"");
    public AgentRoute(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    @Override
    public void configure() throws Exception {
        from("vertx-websocket:/chat")
                .threads(5, 20, "ai-orchestrator-worker")
                .convertBodyTo(String.class)
                // filter
                // Combine predicates using PredicateBuilder.or(...)
                .filter(PredicateBuilder.or(isStatus, isToken, isHandoff, isStreamEnd))
                .log(LoggingLevel.DEBUG, "Dropping notification frame: ${body}")
                .stop()
                .end()
                .process("messageExtractorProcessor")
                .process(exchange -> {
                    String clientId = exchange.getProperty("ClientId", String.class);
                    String connectionKey = exchange.getMessage().getHeader(WEBSOCKET_CONNECTION_KEY, String.class);
                    if(clientId!=null && connectionKey!=null){
                        clientConnections.put(clientId, connectionKey);
                    }
                })
                .log(LoggingLevel.INFO, "Received chat message {}", body().toString())
                .choice()
                .when(exchangeProperty("ActiveDomain").isNotEqualTo("UNASSIGNED"))
                .process(exchange -> {
                    String domain = exchange.getProperty("ActiveDomain", String.class);
                    AgentStrategy strategy = agentRegistry.getStrategy(domain);
                    exchange.setProperty("AgentName", strategy.getAgentName());
                    exchange.setProperty("AgentSystemPrompt", strategy.getSystemPrompt());
                })
                .to("direct:executeStreamingAgent")
                .otherwise()
                .to("direct:classifyAndDispatch")
                .end();


        from("direct:executeStreamingAgent")
                .process("centralizedStreamingAgentProcessor");

        from("direct:classifyAndDispatch")
                // Send Initial Classification Status Frame
                .process(exchange -> {
                    exchange.setProperty("FrameType", "STATUS");
                    exchange.setProperty("FrameMessage", classifierStatusMessage);
                })
                .process("webSocketFrameProcessor") // Processor 1: Frame emitter

                // Invoke Classifier LLM
                .setHeader("CamelLangChain4jAgentSystemMessage", simple(SYSTEM_PROMPT))
                .to("langchain4j-agent:supportOrchestratorAgent")
                .log("Response from support orchestrator agent, ${body}")
                // Dispatch & Bind Selected Agent Strategy
                .process("classificationDispatchProcessor") // Processor 2: Session lock & properties setup
                .process("webSocketFrameProcessor")         // Processor 3: Handoff frame emitter

                // Execute Stream Pipeline
                .to("direct:executeStreamingAgent");

        // Steam response
        from("direct:sendWebSocketResponse")
                .process(exchange -> {
                    JsonNode frame = objectMapper.readTree(exchange.getMessage().getBody(String.class));
                    String clientId = frame.path("clientId").asText(null);
                    String connectionKey = clientId == null ? null : clientConnections.get(clientId);
                    if (connectionKey == null) {
                        throw new IllegalStateException("No WebSocket connection found for clientId: " + clientId);
                    }
                    exchange.getMessage().setHeader(WEBSOCKET_CONNECTION_KEY, connectionKey);
                })
                .to("vertx-websocket:/chat");
    }
}
