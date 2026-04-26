package services.forum;

import entities.forum.SentimentResult;

public interface AIAnalysisStrategy {
    SentimentResult analyze(String text);
}
