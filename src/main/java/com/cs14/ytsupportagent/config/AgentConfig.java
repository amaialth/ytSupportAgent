package com.cs14.ytsupportagent.config;

import dev.langchain4j.model.chat.ChatModel;
import org.apache.camel.component.langchain4j.agent.api.Agent;
import org.apache.camel.component.langchain4j.agent.api.AgentConfiguration;
import org.apache.camel.component.langchain4j.agent.api.AgentWithoutMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean("supportOrchestratorAgent")
    public Agent supportOrchestratorAgent(ChatModel chatModel){
        AgentConfiguration config = new AgentConfiguration()
                .withChatModel(chatModel);
        return new AgentWithoutMemory(config);
    }
}
