package com.sovon9.Simple_LLM_Api.config.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 *  This cladd helps us to implement cross-cutting concerns like auditing how much token is used
 */
public class TokenUsageAuditAdvisor implements CallAdvisor {

    Logger LOGGER = LoggerFactory.getLogger(TokenUsageAuditAdvisor.class);

    // You can check the implementation from
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if(chatResponse.getMetadata()!=null)
        {
            Usage usage = chatResponse.getMetadata().getUsage();
            LOGGER.error("********** request token: =====> "+usage.getPromptTokens());
            LOGGER.error("********** response token: =====> "+usage.getCompletionTokens());
        }
        return chatClientResponse;
    }

    @Override
    public String getName() {
        return "TokenUsageAdvisor";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
