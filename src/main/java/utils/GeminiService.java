package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeminiService {
    private static final String API_KEY = "AIzaSyDXIfuY0hCoi-tmYBWDnd7qeVmCref5l60";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    public static String getGeminiResponse(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Format the JSON request body
            String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                
                // Use regex for more robust parsing of the "text" field
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"text\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);
                
                if (matcher.find()) {
                    String result = matcher.group(1);
                    // Unescape common JSON escapes
                    return result.replace("\\n", "\n")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\")
                                 .replace("\\t", "\t");
                }
                
                return "AI response found but text field missing. Full response: " + responseBody;
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to AI: " + e.getMessage();
        }
    }
}
