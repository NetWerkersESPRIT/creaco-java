package utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApyHubService {
    private static String API_KEY;
    private static final String API_URL = "https://api.apyhub.com/sharpapi/api/v1/content/paraphrase";

    static {
        try {
            // Explicitly load .env from the project root
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();
            
            API_KEY = dotenv.get("PARAPHRASE");
            
            if (API_KEY == null) {
                // Try system environment as fallback
                API_KEY = System.getenv("PARAPHRASE");
            }
            
            if (API_KEY != null) API_KEY = API_KEY.trim();
            
            System.out.println("[AI SERVICE] Key status: " + (API_KEY != null ? "Loaded (" + API_KEY.length() + " chars)" : "NOT FOUND"));
        } catch (Exception e) {
            System.err.println("[AI SERVICE] Error loading environment: " + e.getMessage());
        }
    }

    public static String getParaphrase(String text) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "Error: PARAPHRASE API key not found in .env";
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
            
            // 1. Submit the Job (Strictly following docs)
            String jsonBody = "{"
                + "\"content\": \"" + text.replace("\"", "\\\"").replace("\n", "\\n") + "\","
                + "\"language\": \"English\","
                + "\"voice_tone\": \"professional\""
                + "}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("apy-token", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200 && response.statusCode() != 201 && response.statusCode() != 202) {
                return "API Error (Job Submission): " + response.statusCode() + " - " + response.body();
            }

            // 2. Get the status_url from response
            String responseBody = response.body();
            java.util.regex.Pattern statusUrlPattern = java.util.regex.Pattern.compile("\"status_url\":\\s*\"(.*?)\"");
            java.util.regex.Matcher statusUrlMatcher = statusUrlPattern.matcher(responseBody);
            
            if (!statusUrlMatcher.find()) {
                return "Error: No status_url found. Response: " + responseBody;
            }
            
            String statusUrl = statusUrlMatcher.group(1);
            
            // 3. Poll the status_url
            int attempts = 0;
            while (attempts < 20) { 
                Thread.sleep(2000); 
                attempts++;
                
                HttpRequest statusRequest = HttpRequest.newBuilder()
                        .uri(URI.create(statusUrl))
                        .header("Accept", "application/json")
                        .header("apy-token", API_KEY)
                        .GET()
                        .build();
                
                HttpResponse<String> statusResponse = client.send(statusRequest, HttpResponse.BodyHandlers.ofString());
                String statusBody = statusResponse.body();
                
                // Documentation says Success or queued
                if (statusBody.contains("\"status\":\"success\"") || statusBody.contains("\"status\":\"Success\"") || statusBody.contains("\"status\":\"COMPLETED\"")) {
                    java.util.regex.Pattern resultPattern = java.util.regex.Pattern.compile("\"paraphrased_content\":\\s*\"(.*?)\"(?:\\s*,\"|\\s*})", java.util.regex.Pattern.DOTALL);
                    java.util.regex.Matcher resultMatcher = resultPattern.matcher(statusBody);
                    if (resultMatcher.find()) {
                        String result = resultMatcher.group(1);
                        return result.replace("\\n", "\n")
                                     .replace("\\\"", "\"")
                                     .replace("\\\\", "\\")
                                     .replace("\\t", "\t");
                    }
                } else if (statusBody.contains("\"status\":\"failed\"") || statusBody.contains("\"status\":\"FAILED\"")) {
                    return "AI Job Failed. Response: " + statusBody;
                }
            }
            
            return "Error: Paraphrase job timed out.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to process ApyHub job: " + e.getMessage();
        }
    }
}
