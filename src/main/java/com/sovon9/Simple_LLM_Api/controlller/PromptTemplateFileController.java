package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptTemplateFileController {

    private final ChatClient chatClient;

    @Value("classpath:/promptTemplates/summerizeUserMessage.st")
    private Resource promptTemplate;

    // ChatClient.Builder helps us to create the implementation class object which is DefaultChatClient
    public PromptTemplateFileController(ChatClient.Builder builder)
    {
        this.chatClient = builder
                .build();
    }

    /**
     * The response summerizes the message into bullet points using summerizeUserMessage.st file in resources ^_^
     * @param username
     * @param message
     * @return
     */
    @GetMapping("/file/chat")
    public String chatToLLMUsingTemplate(@RequestParam("username") String username, @RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(promptTemplate)
                        .param("username", username).param("message", message)).call();
        return callResponseSpec.content();
    }

}
