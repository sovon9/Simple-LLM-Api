package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class WebDocRAGController {
    @Autowired
    @Qualifier("webDocChatClient")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @GetMapping("/rag/websearch/chat")
    public ResponseEntity<String> documentChatUsingRAG(@RequestBody String message, @RequestHeader("username") String username)
    {
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .user(message)
                .call();
        return ResponseEntity.ok(callResponseSpec.content());
    }
}
