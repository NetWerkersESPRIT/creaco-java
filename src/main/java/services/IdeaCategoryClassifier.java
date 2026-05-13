package services;

import utils.GeminiService;
import java.util.List;

public class IdeaCategoryClassifier {

    /**
     * Given the new idea's data and a list of existing categories,
     * return the best-fit category name using AI.
     *
     * @param title              The idea title
     * @param description        The idea description
     * @param proposedCategory   The category entered by the user
     * @param existingCategories Distinct categories already in the DB
     * @return The resolved category
     */
    public static String classify(String title, String description, String proposedCategory,
            List<String> existingCategories) {
        String categoryListString = existingCategories.isEmpty()
                ? "(No existing categories yet)"
                : "[" + String.join(", ", existingCategories) + "]";

        String prompt = String.format(
                "You are a category normalization assistant for a collaborative idea platform.\n" +
                        "Your task is to assign the most appropriate category to an idea based on its content.\n\n" +
                        "Rules:\n" +
                        "1. If the proposed category closely matches one of the existing categories (semantically or literally), return EXACTLY that existing category name.\n"
                        +
                        "2. If no existing category fits, return the proposed category as-is OR a cleaner, more professional version of it. Also fix any misspellings or grammatical errors in the proposed category.\n"
                        +
                        "3. Never return more than one category.\n" +
                        "4. Return ONLY the category name — no explanation, no punctuation, no quotes.\n\n" +
                        "Idea Title: %s\n" +
                        "Idea Description: %s\n" +
                        "Proposed Category: %s\n" +
                        "Existing categories to consider: %s\n\n" +
                        "Return ONLY the category name.",
                title, description, proposedCategory, categoryListString);

        try {
            String result = GeminiService.getGeminiResponse(prompt).trim();

            // Safety: if response is empty, too long, or contains an error message, fall
            // back
            if (result.isEmpty() || result.length() > 100 || result.startsWith("API Error")
                    || result.startsWith("Failed to connect")) {
                return proposedCategory;
            }

            return result;
        } catch (Exception e) {
            // On any exception, fall back gracefully
            return proposedCategory;
        }
    }
}
