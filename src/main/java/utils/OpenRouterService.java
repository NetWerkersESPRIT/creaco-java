package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenRouterService {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public static String getAIResponse(String content, String action) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String prompt = "";
            switch (action.toLowerCase()) {
                case "analyze":
                    prompt = "Analyze this forum post and give me the key points: " + content;
                    break;
                case "explain":
                    prompt = "Explain the content of this forum post in very simple terms, as a single short and easy-to-read paragraph: " + content;
                    break;
                case "solution":
                    prompt = "Propose only ONE short, concise, and highly effective solution or piece of advice for this forum post: " + content;
                    break;
                default:
                    prompt = content;
            }

            // JSON body for OpenRouter
            String jsonBody = "{"
                + "\"model\": \"google/gemini-2.0-flash-lite-001\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]"
                + "}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "https://creaco.tn") // Optional for OpenRouter
                    .header("X-Title", "CreaCo Forum") // Optional for OpenRouter
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                
                // OpenRouter returns a chat completion object, we need choices[0].message.content
                // Using a more stack-safe regex to avoid StackOverflowError on long responses
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"content\":\\s*\"(.*?)\"(?:\\s*,\"\\w+\"|\\s*})", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);
                
                if (matcher.find()) {
                    String result = matcher.group(1);
                    return result.replace("\\n", "\n")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\")
                                 .replace("\\t", "\t");
                }
                return "AI Response found but content field missing.";
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to AI: " + e.getMessage();
        }
    }
}
