package services.forum;

import entities.forum.SentimentResult;
import utils.SentimentService;
import java.util.ArrayList;
import java.util.List;

public class UserPostValidator {
    private final List<AIAnalysisStrategy> strategies = new ArrayList<>();

    public UserPostValidator() {
        // Register strategies
        strategies.add(new SentimentService());
    }

    public List<SentimentResult> validate(String text) {
        List<SentimentResult> results = new ArrayList<>();
        for (AIAnalysisStrategy strategy : strategies) {
            results.add(strategy.analyze(text));
        }
        return results;
    }
}
