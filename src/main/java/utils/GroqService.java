package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class GroqService {
    private static String API_KEY;
    private static String SMART_TUTOR_KEY;
    private static String PARAPHRASE_KEY;
    private static String GEMINI_PARAPHRASE_KEY;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            API_KEY = properties.getProperty("GROQ_API_KEY");
            SMART_TUTOR_KEY = properties.getProperty("SMART_TUTOR");
            PARAPHRASE_KEY = properties.getProperty("PARAPHRASE");
            GEMINI_PARAPHRASE_KEY = properties.getProperty("GEMINI_PARAPHRASE");
        } catch (IOException e) {
            System.err.println("Error loading config.properties in GroqService: " + e.getMessage());
        }
    }

    public static String getSmartTutorResponse(String courseName, String prompt) {
        String key = (SMART_TUTOR_KEY != null && !SMART_TUTOR_KEY.isEmpty()) ? SMART_TUTOR_KEY : API_KEY;
        if (key == null || key.isEmpty()) {
            return "Error: No API key found for Smart Tutor.";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String systemPrompt = "You are a helpful and professional AI course tutor for the course: " + courseName + ". " +
                                 "Provide concise, accurate, and encouraging answers to help the student or creator. " +
                                 "If you don't know something specific about the internal course materials, give general expert advice on the topic.";
            
            String jsonBody = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"messages\": ["
                + "{\"role\": \"system\", \"content\": \"" + systemPrompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"},"
                + "{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}"
                + "]"
                + "}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + key)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                return extractResponseContent(root);
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to AI: " + e.getMessage();
        }
    }

    public static String getParaphrase(String text) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: GROQ_API_KEY not found in .env";
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
            return "Error: GROQ_API_KEY not found in .env";
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

    public static String generateQuiz(String resourceContent, int numQuestions) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: GROQ_API_KEY not found in .env";
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = "Generate a quiz with " + numQuestions + " multiple-choice questions based on the following content. " +
                           "Each question should have 4 options (A, B, C, D) and one correct answer. " +
                           "Format the response as a JSON array of objects, each with 'question', 'options' (array of 4 strings), and 'correctAnswer' (index 0-3).\n\n" +
                           "Content: " + resourceContent;

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
                try {
                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                    String content = extractResponseContent(root);
                    String cleaned = cleanJsonArrayString(content);
                    return cleaned;
                } catch (Exception e) {
                    // If the API response is not strictly JSON, fall back to the raw body.
                    return responseBody;
                }
            } else {
                return "API Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to Groq: " + e.getMessage();
        }
    }

    private static String extractResponseContent(JsonObject root) {
        if (root.has("choices") && root.get("choices").isJsonArray()) {
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    if (message.has("content")) {
                        return message.get("content").getAsString();
                    }
                }
                if (firstChoice.has("content")) {
                    return firstChoice.get("content").getAsString();
                }
            }
        }
        if (root.has("content")) {
            return root.get("content").getAsString();
        }
        return root.toString();
    }

    private static String cleanJsonArrayString(String text) {
        if (text == null) {
            return "";
        }
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
