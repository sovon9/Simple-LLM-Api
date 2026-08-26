package com.sovon9.Simple_LLM_Api.model;

import java.util.List;

public record SupportTicket(Category category,
                            Priority priority,
                            Sentiment sentiment,
                            List<String> keyEntities,
                            String summary,
                            String recommendedAction) {
    public enum Category { BILLING, TECHNICAL, ACCOUNT_ACCESS, FEATURE_REQUEST }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Sentiment { POSITIVE, NEUTRAL, FRUSTRATED, ANGRY }
}
