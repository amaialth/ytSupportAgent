package com.cs14.ytsupportagent.agent;

public interface AgentStrategy {
    String getAgentDomain();
    String getAgentName();
    String getSystemPrompt();
    String getHandoffMessage();
}
