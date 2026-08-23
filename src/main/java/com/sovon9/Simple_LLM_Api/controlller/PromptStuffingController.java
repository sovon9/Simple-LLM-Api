package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PromptStuffingController {

    private ChatClient chatClient;

    @Value("classpath:/promptTemplates/systemMessage.st")
    private Resource promptTemplate;

    public PromptStuffingController(ChatClient.Builder builder)
    {
        chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/promptStuffing/chat")
    public String chatToLLMUsingTemplate(@RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .system(promptTemplate)
                .user(message).call();
        return callResponseSpec.content();
    }


}
