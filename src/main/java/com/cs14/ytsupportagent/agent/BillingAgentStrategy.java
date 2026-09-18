package com.cs14.ytsupportagent.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BillingAgentStrategy implements AgentStrategy{
    @Value("${agent.prompts.billing:You are a specialized Billing Support Specialist. Assist with invoice and promo issues.}")
    private String systemPrompt;
    @Value("${agent.handoffs.billing:💳 Handing off control to Billing Agent...}")
    private String handoffMessage;

    @Override
    public String getAgentDomain() {
        return "BILLING";
    }

    @Override
    public String getAgentName() {
        return "Billing Agent";
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
