package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomChatMemoryController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    @Qualifier("customChatMemory")
    private ChatMemory chatMemory;

    /**
     * Method that uses custom CHatMemory object with max messages set to 10
     * @param message
     * @return
     */
    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatWithMemory(@RequestBody String message, @RequestHeader("username") String username)
    {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .advisors(memoryAdvisor)
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call();
        return ResponseEntity.ok(callResponseSpec.content());
    }

}
