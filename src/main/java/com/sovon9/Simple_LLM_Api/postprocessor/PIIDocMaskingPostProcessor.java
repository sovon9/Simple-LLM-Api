package com.sovon9.Simple_LLM_Api.postprocessor;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of DocumentPostProcessor for post-processing of data and remove PII data
 */
public class PIIDocMaskingPostProcessor implements DocumentPostProcessor {

    private final Map<Pattern, String> maskingRules;

    private PIIDocMaskingPostProcessor(Map<Pattern, String> maskingRules) {
        this.maskingRules = maskingRules;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return documents;
        }

        return documents.stream()
                .map(this::maskDocument)
                .collect(Collectors.toList());
        }

        private Document maskDocument(Document document) {
            String originalContent = document.getText();
            if (originalContent == null || originalContent.isBlank()) {
                return document;
            }

            String maskedContent = originalContent;
            for (Map.Entry<Pattern, String> rule : maskingRules.entrySet()) {
                maskedContent = rule.getKey().matcher(maskedContent).replaceAll(rule.getValue());
            }

            // Rebuild the document to preserve ID and Metadata while updating the content
            return document.mutate()
                    .text(maskedContent)
                    .build();
        }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final Map<Pattern, String> maskingRules = new HashMap<>();
        private Builder()
        {

            // Email Masking
            maskingRules.put(Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"), "[EMAIL_REDACTED]");

            // US Phone Number Masking (e.g., 555-555-5555, 555.555.5555, 5555555555)
            maskingRules.put(Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"), "[PHONE_REDACTED]");

            // SSN Masking (e.g., 123-45-6789)
            maskingRules.put(Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"), "[SSN_REDACTED]");
        }

        public Builder addRule(String regex, String replacementText) {
            this.maskingRules.put(Pattern.compile(regex), replacementText);
            return this;
        }

        public Builder clearDefaultRules() {
            this.maskingRules.clear();
            return this;
        }

        public PIIDocMaskingPostProcessor build()
        {
            return new PIIDocMaskingPostProcessor(this.maskingRules);
        }
    }
}
