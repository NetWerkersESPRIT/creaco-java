package services.forum;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamDetectionService {

    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
            "casino", "porn", "betting", "gambling", "crypto", "bitcoin", "lottery", "winner", "prize"
    );

    private static final List<String> PROMOTION_KEYWORDS = Arrays.asList(
            "buy now", "free offer", "earn money", "make money", "discount", "click here", "best price", "cheap"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(http|https)://[a-zA-Z0-9./?=-]+", Pattern.CASE_INSENSITIVE
    );

    /**
     * Calculates a spam score from 0 to 100 based on text analysis.
     * @param text The content to analyze.
     * @return An integer score between 0 and 100.
     */
    public int calculateSpamScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        int score = 0;
        String lowerText = text.toLowerCase();

        // 1. Link Detection (Max 40 points)
        Matcher matcher = URL_PATTERN.matcher(text);
        int linkCount = 0;
        while (matcher.find()) {
            linkCount++;
        }
        if (linkCount > 0) {
            score += Math.min(linkCount * 15, 40);
        }

        // 2. Forbidden Words Detection (Max 30 points)
        for (String word : FORBIDDEN_WORDS) {
            if (lowerText.contains(word)) {
                score += 15;
            }
        }
        score = Math.min(score, 70); // Cap after first two checks

        // 3. Promotion Keywords (Max 30 points)
        for (String keyword : PROMOTION_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                score += 10;
            }
        }

        // 4. Word Repetition (Max 20 points)
        String[] words = lowerText.split("\\s+");
        if (words.length > 10) {
            long distinctCount = Arrays.stream(words).distinct().count();
            double ratio = (double) distinctCount / words.length;
            if (ratio < 0.4) { // Less than 40% unique words
                score += 20;
            } else if (ratio < 0.6) {
                score += 10;
            }
        }

        return Math.min(score, 100);
    }

    /**
     * Returns a human-readable status based on the spam score.
     * @param score The spam score (0-100).
     * @return Status string.
     */
    public String getSpamStatus(int score) {
        if (score >= 80) return "SPAM";
        if (score >= 40) return "SUSPICIOUS";
        return "CLEAN";
    }
}
