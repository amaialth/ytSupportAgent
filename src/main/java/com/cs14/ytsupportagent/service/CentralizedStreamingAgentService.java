package com.cs14.ytsupportagent.service;

import dev.langchain4j.service.*;

public interface CentralizedStreamingAgentService {

    @SystemMessage("{{systemPrompt}}")
    TokenStream chat(@UserName String clientId,
                     @V("systemPrompt") String systemPrompt,
                     @UserMessage String userMessage);
}
