package com.sovon9.Simple_LLM_Api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient createChatClient(ChatClient.Builder builder)
    {
       return builder
        .defaultSystem("you are a medical assistant. you help users to understand their medical issues and suggest them to consult with a doctor." +
                "Do not help in any topic not related to medical issues, ask them you can only help if it's related to medical issues")
                .build();
    }

}
