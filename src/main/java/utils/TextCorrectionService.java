package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

public class TextCorrectionService {
    /** Circuit-breaker: skip all calls once we know credits are exhausted. */
    private static volatile boolean apiUnavailable = false;
    private static String API_KEY;
    private static final String API_URL = "https://api.apiverve.com/v1/spellchecker";

    static {
        Properties properties = new Properties();
        java.io.File configFile = new java.io.File("config.properties");
        System.out.println("[TextCorrectionService] Looking for config at: " + configFile.getAbsolutePath());
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                API_KEY = properties.getProperty("APIVERVE_API_KEY");
                System.out.println("[TextCorrectionService] API Key loaded successfully.");
            } catch (IOException e) {
                System.err.println("Error loading config.properties in TextCorrectionService: " + e.getMessage());
            }
        } else {
            System.err.println("[TextCorrectionService] config.properties NOT FOUND!");
        }
    }

    /**
     * Corrects spelling errors in the provided text using ApiVerve Spell Checker API.
     * @param text The text to correct.
     * @return The corrected text, or the original text if correction fails.
     */
    public static String correctText(String text) {
        if (text == null || text.trim().isEmpty() || apiUnavailable) {
            return text;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
            JSONObject bodyObj = new JSONObject();
            bodyObj.put("text", text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("x-api-key", API_KEY)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyObj.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[TextCorrectionService] API Response: " + response.body());

            JSONObject jsonResponse = new JSONObject(response.body());
            // Detect credit exhaustion and trip circuit-breaker
            if ("error".equals(jsonResponse.optString("status")) &&
                    jsonResponse.optString("error", "").contains("credit")) {
                apiUnavailable = true;
                System.err.println("[TextCorrectionService] Credit limit reached — disabling API calls.");
                return text;
            }

            if (response.statusCode() == 200 && "ok".equals(jsonResponse.optString("status"))) {
                {
                    /**this contains:
                    //corrections list
                   words to fix */
                    Object dataObj = jsonResponse.get("data");
                    if (dataObj instanceof JSONObject) {
                        JSONObject data = (JSONObject) dataObj;
                        JSONArray corrections = data.optJSONArray("corrections");
                        if (corrections == null) return text;
                        
                        StringBuilder correctedText = new StringBuilder(text);
                        for (int i = corrections.length() - 1; i >= 0; i--) {
                            JSONObject corr = corrections.getJSONObject(i);
                            String original = corr.getString("word");
                            JSONArray suggestions = corr.optJSONArray("suggestions");
                            
                            if (suggestions != null && suggestions.length() > 0) {
                                String replacement = suggestions.getString(0);
                                int lastIndex = correctedText.lastIndexOf(original);
                                if (lastIndex != -1) {
                                    correctedText.replace(lastIndex, lastIndex + original.length(), replacement);
                                }
                            }
                        }
                        return correctedText.toString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Text correction with ApiVerve failed: " + e.getMessage());
        }
        return text; // Fallback to original text
    }
}
