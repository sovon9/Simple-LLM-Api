package com.sovon9.Simple_LLM_Api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VectorDataLoader {

    private final VectorStore vectorStore;

    public VectorDataLoader(VectorStore vectorStore)
    {
        this.vectorStore=vectorStore;
    }

    @PostConstruct
    public void loadDataToVectorDB()
    {
        List<String> dataList = List.of("The company Sovon9 only allows 10 days holiday.",
                "employees have 10 sick leave, 10 earned leave and 5 casual leaves.",
                "To apply for leave you can check out company HRHelp site for any HR related details.",
                "employees salary will be credited 1st of every month.",
                "The company Sovon9 works with latest technologies in software business with more than 20 years of experience.",
                "For any hea;th related issues please reach out to your floor supervisor and he will help with all the health related things.",
                "Every employee is entitled with a food card which can be used for buying food in office canteen.",
                "Every employee must adhere all security related rules for client data, always follow best practices related to data security.",
                "Employees must collect their id cards from ground floor of Main security building of the office.");

        List<Document> documentList = dataList.stream().map(Document::new).collect(Collectors.toList());
        vectorStore.add(documentList);
    }


}
