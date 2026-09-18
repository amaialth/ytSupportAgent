package com.cs14.ytsupportagent.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TechnicalAgentStrategy implements AgentStrategy {

    @Value("${agent.prompts.technical:You are a Technical Support Specialist. Assist with system errors and bug reports.}")
    private String systemPrompt;

    @Value("${agent.handoffs.technical:🔧 Connecting you with Technical Support...}")
    private String handoffMessage;

    @Override
    public String getAgentDomain() {
        return "TECHNICAL_ISSUE";
    }

    @Override
    public String getAgentName() {
        return "Technical Agent";
    }

    @Override
    public String getSystemPrompt() {
        return systemPrompt;
    }

    @Override
    public String getHandoffMessage() {
        return handoffMessage;
    }
}