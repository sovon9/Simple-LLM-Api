package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StreamController {

    @Autowired
    private ChatClient chatClient;

    /**
     *  We will get the message as stream/ flux which means it's non-blocking. we will get the messages one by one as the LOLm generates it
     *  You need to try it in browser as postman doesn't support stream
     * @param message
     * @return
     */
    @GetMapping("/stream/chat")
    public Flux<String> chatInStream(@RequestBody String message)
    {
        ChatClient.StreamResponseSpec streamResponseSpec = chatClient.prompt()
                .user(message).stream();
        return streamResponseSpec.content();
    }
}
