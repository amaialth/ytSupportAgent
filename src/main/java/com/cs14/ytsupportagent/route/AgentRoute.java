package com.cs14.ytsupportagent.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AgentRoute extends RouteBuilder {
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
    @Override
    public void configure() throws Exception {
        from("vertx-websocket:/chat")
                .setHeader("CamelLangChain4jAgentSystemMessage", constant(SYSTEM_PROMPT))
                .to("langchain4j-agent:supportOrchestratorAgent")
                .to("vertx-websocket:/chat");
    }
}
