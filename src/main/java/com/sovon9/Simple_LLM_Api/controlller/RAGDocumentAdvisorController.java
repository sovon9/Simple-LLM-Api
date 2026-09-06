package com.sovon9.Simple_LLM_Api.controlller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is to demonstrate how to use chatClient obj which is already configured with
 */
@RestController
public class RAGDocumentAdvisorController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @Value("classpath:/promptTemplates/systemRAGMessage.st")
    private Resource systemRAGMessage;

    /**
     * Method that uses custom CHatMemory object with max messages set to 10
     * @param message
     * @return
     */
//    @GetMapping("/rag/doc/chat")
//    public ResponseEntity<String> documentChatUsingRAG(@RequestBody String message, @RequestHeader("username") String username)
//    {
//        // to get relevant data from vector DB we need to create a SearchRequest object with certain parameters
//        SearchRequest searchRequest = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5).build();
//
//        // using the searchRequest object we can query the vectorStore
//        List<Document> similarSearch = vectorStore.similaritySearch(searchRequest);
//
//        String similarContext = similarSearch.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
//
//        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
//                .system(promptSystemSpec -> promptSystemSpec.text(systemRAGMessage)
//                        .param("documents", similarContext))
//                .user(message)
//                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
//                .call();
//        return ResponseEntity.ok(callResponseSpec.content());
//    }

    @GetMapping("/rag/doc/advisor/chat")
    public ResponseEntity<String> documentChatUsingRAG(@RequestBody String message, @RequestHeader("username") String username)
    {
//        SearchRequest searchRequest = SearchRequest.builder().query(message).topK(3).similarityThreshold(0.5).build();
//        List<Document> similarSearch = vectorStore.similaritySearch(searchRequest);
//        String similarContext = similarSearch.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));

        /**
         *  since we are using RetrievalAugmentationAdvisor in configuring chatclient so we no need to manually write above
         *  codes to search thr data and set the system message.
         */
        ChatClient.CallResponseSpec callResponseSpec = chatClient.prompt()
//                .system(promptSystemSpec -> promptSystemSpec.text(systemRAGMessage)
//                        .param("documents", similarContext))
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .call();
        return ResponseEntity.ok(callResponseSpec.content());
    }


}
