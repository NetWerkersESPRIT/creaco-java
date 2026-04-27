package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroqAssistService {
    // API key and endpoint are managed centrally in GroqConfig.
    // To update your key go to: utils/GroqConfig.java
    private static final String API_KEY = GroqConfig.API_KEY;
    private static final String API_URL = GroqConfig.API_URL;

    public static String getFormalBusinessText(String context, String originalText) {
        if (originalText == null || originalText.trim().isEmpty()) {
            return originalText;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            String systemPrompt = "You are a professional business consultant and contract expert. " +
                    "Your task is to rewrite the provided rough or informal text into highly formal, authoritative, and convincing business language. "
                    +
                    "Remove conversational filler and use sophisticated vocabulary appropriate for a business contract. "
                    +
                    "The text pertains to the '" + context + "' section of a collaboration proposal. " +
                    "Provide ONLY the rewritten text without any additional commentary, introduction, or markdown formatting.";

            // Escaping for JSON
            String safeSystemPrompt = systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            String safeOriginalText = originalText.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");

            String jsonBody = "{"
                    + "\"model\": \"" + GroqConfig.ASSIST_MODEL + "\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"" + safeSystemPrompt + "\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + safeOriginalText + "\"}"
                    + "],"
                    + "\"temperature\": 0.3"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                // Simple regex to extract the content field from the JSON response
                Pattern pattern = Pattern.compile("\"content\":\\s*\"(.*?)\"(?:\\s*,\"\\w+\"|\\s*})", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(responseBody);

                if (matcher.find()) {
                    String result = matcher.group(1);
                    return result.replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .replace("\\t", "\t")
                            .trim();
                }
                return "Failed to parse API response.";
            } else {
                System.err.println("Groq API Error: " + response.statusCode() + " - " + response.body());
                return originalText; // Return original if API fails
            }
        } catch (Exception e) {
            e.printStackTrace();
            return originalText; // Return original on error
        }
    }
}
