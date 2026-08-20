package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatControllerWithDefaults {

    // ChatClient is a high level interface helps us to interact with ChatModel
    private final ChatClient chatClient;

    // ChatClient.Builder helps us to create the implementation class object which is DefaultChatClient
    public ChatControllerWithDefaults(ChatClient.Builder builder)
    {
        this.chatClient = builder.
                defaultSystem("you are a medical assistant. you help users to understand their medical issues and suggest them to consult with a doctor." +
                        "Do not help in any topic not related to medical issues, ask them you can only help if it's related to medical issues")
                .build();
    }

    @GetMapping("/role/default/chat")
    public String chatToLLMWithRole(@RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .user(message).call();
        return callResponseSpec.content();
    }

}
