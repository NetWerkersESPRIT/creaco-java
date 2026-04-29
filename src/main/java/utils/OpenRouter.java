package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OpenRouter {
    public static final String API_KEY;
    public static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    public static final String DEFAULT_MODEL = "openrouter/auto";

    static {
        Properties properties = new Properties();
        String key = null;
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            key = properties.getProperty("OPENROUTER_API_KEY");
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
        API_KEY = key;
    }

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getAIResponse(String content, String action) {
        if (!isConfigured()) {
            return "Error: OpenRouter API key is missing. Please check config.properties.";
        }

        try {
            String prompt = buildPrompt(content, action);

            ObjectNode body = mapper.createObjectNode();
            body.put("model", DEFAULT_MODEL);

            ArrayNode messages = mapper.createArrayNode();
            ObjectNode message = mapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt);

            messages.add(message);
            body.set("messages", messages);

            String jsonBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "Creaco Forum")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return extractContent(response.body());
            } else {
                return "API Error: " + response.statusCode() + "\n" + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Request failed: " + e.getMessage();
        }
    }

    private static String buildPrompt(String content, String action) {
        if (action == null) return content;

        switch (action.toLowerCase()) {
            case "explain":
                return "Explain the content of this forum post in very simple terms, as a single short and easy-to-read paragraph:\n" + content;
            case "solution":
                return "Propose only ONE short, concise, and highly effective solution or piece of advice for this forum post:\n" + content;
            default:
                return content;
        }
    }

    private static String extractContent(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText("No content returned.");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    public static boolean isConfigured() {
        return API_KEY != null && !API_KEY.trim().isEmpty() && !API_KEY.contains("YOUR_");
    }
}
