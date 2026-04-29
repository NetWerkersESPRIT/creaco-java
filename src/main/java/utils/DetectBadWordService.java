package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class DetectBadWordService {
    private static String API_KEY;
    private static final String API_URL = "https://api.apiverve.com/v1/profanityfilter";

    static {
        Properties properties = new Properties();
        java.io.File configFile = new java.io.File("config.properties");
        System.out.println("[DetectBadWordService] Looking for config at: " + configFile.getAbsolutePath());
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                API_KEY = properties.getProperty("APIVERVE_API_KEY");
                System.out.println("[DetectBadWordService] ApiVerve API Key loaded successfully.");
            } catch (IOException e) {
                System.err.println("Error loading config.properties in DetectBadWordService: " + e.getMessage());
            }
        } else {
            System.err.println("[DetectBadWordService] config.properties NOT FOUND!");
        }
    }

    public static class ModerationResult {
        public String moderatedText;
        public int profaneWordsCount;
        public int grammarErrorsCount;
        public boolean isProfane;

        public ModerationResult(String moderatedText, int profaneWordsCount, int grammarErrorsCount) {
            this.moderatedText = moderatedText;
            this.profaneWordsCount = profaneWordsCount;
            this.grammarErrorsCount = grammarErrorsCount;
            this.isProfane = profaneWordsCount > 0;
        }
    }

    /**
     * Moderates the text by detecting profanity and grammar errors.
     * Replaces bad words with ****.
     */
    public static CompletableFuture<ModerationResult> moderate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return CompletableFuture.completedFuture(new ModerationResult(text, 0, 0));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                
                JSONObject bodyObj = new JSONObject();
                bodyObj.put("text", text);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("x-api-key", API_KEY)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(bodyObj.toString(), StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("[DetectBadWordService] API Response: " + response.body());

                String moderatedText = text;
                int profanityCount = 0;

                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    if ("ok".equals(json.getString("status"))) {
                        JSONObject data = json.getJSONObject("data");
                        moderatedText = data.getString("filteredText");
                        profanityCount = data.getInt("profaneWords");
                        System.out.println("[DetectBadWordService] Found " + profanityCount + " bad words.");
                    }
                }

                return new ModerationResult(moderatedText, profanityCount, 0); // Grammar errors not checked here anymore

            } catch (Exception e) {
                System.err.println("Moderation failed: " + e.getMessage());
                return new ModerationResult(text, 0, 0);
            }
        });
    }
}
