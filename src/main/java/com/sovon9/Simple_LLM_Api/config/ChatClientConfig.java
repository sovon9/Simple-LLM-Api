package com.sovon9.Simple_LLM_Api.config;

import com.sovon9.Simple_LLM_Api.config.advisor.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient createChatClient(ChatClient.Builder builder)
    {
       return builder
               .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor()))
               .defaultOptions(ChatOptions.builder().model(GoogleGenAiChatModel.ChatModel.GEMINI_3_1_FLASH_LITE.value).temperature(0.6))
               .defaultSystem("you are a medical assistant. you help users to understand their medical issues and suggest them to consult with a doctor." +
                        "Do not help in any topic not related to medical issues, ask them you can only help if it's related to medical issues")
               .build();
    }

}
