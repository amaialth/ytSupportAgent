package com.cs14.ytsupportagent.agent;

import org.springframework.stereotype.Component;

@Component
public class GeneralAgentStrategy implements AgentStrategy {

    private static final String GENERAL_PROMPT = """
        You are a General Customer Support Agent.
        Provide helpful, polite, and accurate assistance for general inquiries.
        """;

    @Override
    public String getAgentDomain() {
        return "GENERAL";
    }

    @Override
    public String getAgentName() {
        return "General Agent";
    }

    @Override
    public String getSystemPrompt() {
        return GENERAL_PROMPT;
    }

    @Override
    public String getHandoffMessage() {
        return "ℹ️ Transferring to General Support Agent...";
    }
}
