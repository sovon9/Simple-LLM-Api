package com.sovon9.Simple_LLM_Api.config;

import com.sovon9.Simple_LLM_Api.config.advisor.TokenUsageAuditAdvisor;
import com.sovon9.Simple_LLM_Api.webdoc.WebSearchRAGDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class WebSearchRAGConfig {

    @Bean
    public RestClient.Builder restClientBuilder()
    {
        return RestClient.builder();
    }

    /**
     * This chatClient obj is with custom document retriever which fetches doc from web using Tavily
     * @param builder
     * @param chatMemory
     * @param retrievalAugmentationAdvisor
     * @param restClientBuilder
     * @param env
     * @return
     */
    @Bean("webDocChatClient")
    public ChatClient createChatClient(ChatClient.Builder builder, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
                                       RestClient.Builder restClientBuilder, Environment env)
    {
        // Advisor for chat memory
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        // custom advisor for web search
        WebSearchRAGDocumentRetriever documentRetriever = WebSearchRAGDocumentRetriever.builder().restClient(restClientBuilder).maxResult(5).environment(env).build();
        RetrievalAugmentationAdvisor webDocAdvisor = RetrievalAugmentationAdvisor.builder().documentRetriever(documentRetriever).build();

        // build the ChatClient object, webDocAdvisor is added for web search
        return builder
                .defaultAdvisors(List.of(memoryAdvisor, new SimpleLoggerAdvisor(),
                        new TokenUsageAuditAdvisor(), retrievalAugmentationAdvisor, webDocAdvisor))
                .build();
    }

}
