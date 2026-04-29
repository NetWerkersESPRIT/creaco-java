package utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GroqService {
    private static String API_KEY;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    static {
        try {
            Dotenv dotenv = Dotenv.load();
            API_KEY = dotenv.get("SMART_TUTOR");
        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }

    public static String getParaphrase(String text) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: SMART_TUTOR API key not found in .env";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String prompt = "Paraphrase the following text while maintaining its core meaning and making it sound professional and engaging. Return ONLY the paraphrased text:\n\n" + text;
            
            // OpenAI compatible JSON body
            String jsonBody = "{"
                + "\"model\": \"llama-3.1-8b-instant\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]"
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
                
                // Extract "content" from OpenAI response format
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"content\":\\s*\"(.*?)\"(?:\\s*,\"|\\s*})", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);
                
                if (matcher.find()) {
                    String result = matcher.group(1);
                    return result.replace("\\n", "\n")
                                 .replace("\\\"", "\"")
                                 .replace("\\\\", "\\")
                                 .replace("\\t", "\t");
                }
                return "AI response found but content field missing.";
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to AI: " + e.getMessage();
        }
    }
    public static String generateCourseIdea(String topic) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: SMART_TUTOR API key not found in .env";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String prompt = "You are an expert course creator. The user wants to create a course about: " + topic + ". Generate a catchy Course Title and a professional, engaging description (max 2 paragraphs). Return ONLY the content in this format:\nTITLE: [The Title]\nDESCRIPTION: [The Description]";
            
            String jsonBody = "{"
                + "\"model\": \"llama-3.1-8b-instant\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]"
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
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"content\":\\s*\"(.*?)\"(?:\\s*,\"|\\s*})", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(responseBody);
                if (matcher.find()) {
                    String result = matcher.group(1);
                    return result.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\").replace("\\t", "\t");
                }
                return "AI response found but content field missing.";
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to AI: " + e.getMessage();
        }
    }
}
