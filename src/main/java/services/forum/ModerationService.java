package services.forum;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ModerationService {
    // Predefined list of bad words (can be expanded)
    private static final List<String> BAD_WORDS = Arrays.asList(
        "test", "badword1", "badword2", "spam", "offensive", "profane", "toxic"
    );

    /**
     * Filters profanity from the text and returns the cleaned version.
     * Replaces bad words with "***".
     */
    public static String filterProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 1. Local filter
        String cleanedText = text;
        for (String word : BAD_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            cleanedText = cleanedText.replaceAll(regex, "***");
        }

        // 2. API filter (additional check)
        try {
            utils.DetectBadWordService.ModerationResult apiResult = utils.DetectBadWordService.moderate(cleanedText).join();
            if (apiResult != null && apiResult.moderatedText != null) {
                cleanedText = apiResult.moderatedText;
            }
        } catch (Exception e) {
            System.err.println("[ModerationService] API Filter failed, using local result: " + e.getMessage());
        }

        return cleanedText;
    }

    /**
     * Checks if the text contains any profanity.
     */
    public static boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : BAD_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            if (Pattern.compile(regex).matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts how many profane words are present in the text.
     */
    public static int countProfaneWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String word : BAD_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            java.util.regex.Matcher matcher = Pattern.compile(regex).matcher(text);
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }
}
