package com.sovon9.Simple_LLM_Api.config;

import com.sovon9.Simple_LLM_Api.config.advisor.TokenUsageAuditAdvisor;
import com.sovon9.Simple_LLM_Api.postprocessor.PIIDocMaskingPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient createChatClient(ChatClient.Builder builder, ChatMemory chatMemory, RetrievalAugmentationAdvisor retrievalAugmentationAdvisor)
    {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return builder
               .defaultAdvisors(List.of(memoryAdvisor, new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor(), retrievalAugmentationAdvisor))
               .build();
    }

    /**
     *
     * @param vectorStore
     * @return
     */
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore, ChatClient.Builder chatClientBuilder)
    {
        return RetrievalAugmentationAdvisor.builder()
                // to pre retrieve the data and process it before sending to Vector DB for retrieval
                .queryTransformers(CompressionQueryTransformer.builder().chatClientBuilder(chatClientBuilder.clone()).build())
                .documentRetriever(
                VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
                        .topK(3).similarityThreshold(0.5).build())
                // --- document post processing ---
                //.documentPostProcessors(PIIDocMaskingPostProcessor.builder().build())
                .build();
    }

}
