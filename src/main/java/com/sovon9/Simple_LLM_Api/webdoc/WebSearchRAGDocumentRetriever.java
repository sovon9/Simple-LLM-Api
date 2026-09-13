package com.sovon9.Simple_LLM_Api.webdoc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

/**
 * custom implementation of DocumentRetriever interface to retrieve information from web
 */
public class WebSearchRAGDocumentRetriever implements DocumentRetriever {

    Logger LOGGER = LoggerFactory.getLogger(WebSearchRAGDocumentRetriever.class);

    private final String TAVILY_API_URL = "https://api.tavily.com/search";

    private final RestClient restClient;

    private final int maxResult;

    public WebSearchRAGDocumentRetriever(RestClient.Builder builder, int maxResult, Environment env) {
        this.restClient = builder.baseUrl(TAVILY_API_URL)
                .defaultHeader("Authorization", "Bearer "+env.getProperty("tavily_api_key"))
                .build();
        this.maxResult=maxResult;
    }

    @Override
    public List<Document> retrieve(Query query) {
        LOGGER.info("Processing query: {}", query.text());
        Assert.notNull(query, "query cannot be null");

        String q = query.text();
        Assert.hasText(q, "query.text() cannot be empty");

        TavilyResponsePayload response = restClient.post()
                .body(new TavilyRequestPayload(q, "advanced", maxResult))
                .retrieve()
                .body(TavilyResponsePayload.class);

        if (response == null || CollectionUtils.isEmpty(response.results())) {
            return List.of();
        }

        List<Document> docs = new ArrayList<>(response.results().size());
        for (TavilyResponsePayload.Hit hit : response.results()) {
            // Map each Tavily hit into a Spring AI Document with metadata and score.
            Document doc = Document.builder()
                    .text(hit.content())
                    .metadata("title", hit.title())
                    .metadata("url", hit.url())
                    .score(hit.score())
                    .build();
            docs.add(doc);
        }
        return docs;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record TavilyRequestPayload(String query, String searchDepth, int maxResult){};

    record TavilyResponsePayload(List<Hit> results){
        record Hit(String title, String url, String content, Double score) {}
    };

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private int maxResult;
        private RestClient.Builder builder;
        private Environment environment;

        private Builder()
        {

        }

        public Builder maxResult(int maxResult)
        {
            this.maxResult=maxResult;
            return this;
        }

        public Builder restClient(RestClient.Builder builder)
        {
            this.builder=builder;
            return this;
        }

        public Builder environment(Environment env)
        {
            this.environment = env;
            return this;
        }

        public WebSearchRAGDocumentRetriever build()
        {
            return new WebSearchRAGDocumentRetriever(this.builder, this.maxResult, this.environment);
        }

    }
}
