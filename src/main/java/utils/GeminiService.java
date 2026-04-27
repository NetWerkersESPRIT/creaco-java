package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class GeminiService {
    private static String API_KEY;
    // Using gemini-2.5-flash as 1.5-flash is now legacy/retired
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    static {
        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
            API_KEY = dotenv.get("GEMINI_PARAPHRASE");
            if (API_KEY != null) API_KEY = API_KEY.trim();
        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
        
        if (API_KEY == null) {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                properties.load(fis);
                API_KEY = properties.getProperty("GEMINI_PARAPHRASE");
            } catch (IOException e) {
                // Fallback to system environment
                API_KEY = System.getenv("GEMINI_PARAPHRASE");
            }
        }
    }

    public static String getParaphrase(String text) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: GEMINI_PARAPHRASE API key not found in .env";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Format the JSON request body
            String prompt = "Paraphrase the following text while keeping its original meaning: " + text;
            String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
            
            // Add API key as query parameter as per user suggestion
            String urlWithKey = API_URL + "?key=" + API_KEY;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlWithKey))
                    .header("Content-Type", "application/json")
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
