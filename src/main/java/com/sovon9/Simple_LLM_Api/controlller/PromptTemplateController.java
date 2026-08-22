package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptTemplateController {

    private final ChatClient chatClient;

    private String promptTemplate= """
                The user {username} whats a summery of the following message "{message}".
                Respond with a summerized version of the message with bullet points and important words
                marked in bold.
            """;

    // ChatClient.Builder helps us to create the implementation class object which is DefaultChatClient
    public PromptTemplateController(ChatClient.Builder builder)
    {
        this.chatClient = builder
                .build();
    }

    /**
     * The response summerizes the message into bullet points ^_^
     * @param username
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public String chatToLLMUsingTemplate(@RequestParam("username") String username, @RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(promptTemplate)
                        .param("username", username).param("message", message)).call();
        return callResponseSpec.content();
    }

}
