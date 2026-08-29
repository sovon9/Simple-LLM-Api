package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatMemoryController {

    @Autowired
    private ChatClient chatClient;

    /**
     *
     * @param message
     * @return
     */
    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatWithMemory(@RequestBody String message, @RequestHeader("username") String username)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call();
        return ResponseEntity.ok(callResponseSpec.content());
    }

}
