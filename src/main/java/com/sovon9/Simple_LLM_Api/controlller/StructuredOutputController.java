package com.sovon9.Simple_LLM_Api.controlller;

import com.sovon9.Simple_LLM_Api.model.SupportTicket;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StructuredOutputController {

    private ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder builder)
    {
        chatClient = builder
                .defaultSystem("""
                        you are a IT support agent. You help uwith user's IT related issues.
                        create a support ticket based of the user message and assign proper details like 
                        catagory, priority, sentiment, keyEntities, summery, recommendedAction.
                        If any issue asked not related to IT just mention you can't help them with that.
                        """)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     *
     * @param message
     * @return
     */
    @GetMapping("/structured/chat")
    public ResponseEntity<SupportTicket> chatInStream(@RequestBody String message)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .user(message).call();
        SupportTicket entity = callResponseSpec.entity(SupportTicket.class);
        return ResponseEntity.ok(entity);
    }

}
