package com.sovon9.Simple_LLM_Api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PdfDataLoader {

    private final VectorStore vectorStore;

    @Value("classpath:/static/Sovon9_HR_Policy_Handbook.pdf")
    private Resource hrHandbook;

    public PdfDataLoader(VectorStore vectorStore)
    {
        this.vectorStore=vectorStore;
    }

    @PostConstruct
    public void loadPdf()
    {
        TikaDocumentReader documentReader = new TikaDocumentReader(hrHandbook);
        List<Document> dataList = documentReader.get();
        TextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(100).withMaxNumChunks(400).build();
        vectorStore.add(textSplitter.split(dataList));
    }


}
