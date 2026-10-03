package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {

    // ChatClient is a high level interface helps us to interact with ChatModel
    private final ChatClient chatClient;

    // ChatClient.Builder helps us to create the implementation class object which is DefaultChatClient
    public ChatController(ChatClient chatClient)
    {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public String chatToLLM(@RequestBody String message, @RequestHeader("username") String username)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient
                .prompt()
                .user(message)
                .toolContext(Map.of("username", username))
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call();
        // CallResponseSpec gives a lot of details related to many specifications of the response like model and all
        // to get only the response from it we can use content()
        return callResponseSpec.content();
    }

}
