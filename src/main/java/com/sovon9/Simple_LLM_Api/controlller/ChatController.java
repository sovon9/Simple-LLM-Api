package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    // ChatClient is a high level interface helps us to interact with ChatModel
    private final ChatClient chatClient;

    // ChatClient.Builder helps us to create the implementation class object which is DefaultChatClient
    public ChatController(ChatClient.Builder builder)
    {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    public String chatToLLM(@RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt(message).call();
        // CallResponseSpec gives a lot of details related to many specifications of the response like model and all
        // to get only the response from it we can use content()
        return callResponseSpec.content();
    }

    @GetMapping("/role/chat")
    public String chatToLLMWithRole(@RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .system("you are a medical assistant. you help users to understand their medical issues and suggest them to consult with a doctor." +
                        "Do not help in any topic not related to medical issues, ask them you can only help if it's related to medical issues")
                .user(message).call();
        // CallResponseSpec gives a lot of details related to many specifications of the response like model and all
        // to get only the response from it we can use content()
        return callResponseSpec.content();
    }

}
