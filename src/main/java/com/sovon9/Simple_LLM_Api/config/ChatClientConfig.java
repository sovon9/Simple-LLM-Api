package com.sovon9.Simple_LLM_Api.config;

import com.sovon9.Simple_LLM_Api.config.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient createChatClient(ChatClient.Builder builder, ChatMemory chatMemory)
    {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return builder
               .defaultAdvisors(List.of(memoryAdvisor, new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor()))
               .build();
    }

    // if you want customization then need to create a chatMemory object
    // here max messages to keep in memory is set to 10
//    @Bean("customChatMemory")
//    public ChatMemory chatMemory(JdbcChatMemoryRepository chatMemoryRepository)
//    {
//        return MessageWindowChatMemory.builder().maxMessages(10).chatMemoryRepository(chatMemoryRepository).build();
//    }

}
