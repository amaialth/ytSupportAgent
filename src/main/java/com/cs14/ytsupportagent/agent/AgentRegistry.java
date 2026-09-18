package com.cs14.ytsupportagent.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentRegistry {
    private final Map<String, AgentStrategy> strategies = new ConcurrentHashMap<>();
    private final Map<String, String> sessionLocks = new ConcurrentHashMap<>();

    public AgentRegistry(List<AgentStrategy> agentStrategies){
        for(AgentStrategy strategy: agentStrategies){
            strategies.put(strategy.getAgentDomain(), strategy);
        }
    }

    public AgentStrategy getStrategy(String domain){
        return strategies.getOrDefault(domain, strategies.get("GENERAL"));
    }

    public String getActiveDomain(String clientId){
        return sessionLocks.getOrDefault(clientId, "UNASSIGNED");
    }

    public void lockSession(String clientId, String domain){
        sessionLocks.put(clientId, domain);
    }
}
