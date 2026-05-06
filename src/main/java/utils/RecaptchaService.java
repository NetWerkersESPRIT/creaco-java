package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class RecaptchaService {
    private static String API_KEY;
    private static String PROJECT_ID = "java-1777469158652";
    private static String SITE_KEY = "6LfEfNAsAAAAAAMtRP2CvVFSK1wzh593gDWdkcta";
    private static String API_URL;

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            API_KEY = properties.getProperty("GEMINI_API_KEY");
            // Load project id and site key from config, fallback to hardcoded if missing
            String configProjectId = properties.getProperty("RECAPTCHA_PROJECT_ID");
            if (configProjectId != null) PROJECT_ID = configProjectId;
            
            String configSiteKey = properties.getProperty("RECAPTCHA_SITE_KEY");
            if (configSiteKey != null) SITE_KEY = configSiteKey;
            
            API_URL = "https://recaptchaenterprise.googleapis.com/v1/projects/" + PROJECT_ID + "/assessments?key=";
        } catch (IOException e) {
            System.err.println("[RecaptchaService] Error loading config.properties: " + e.getMessage());
        }
    }

    /**
     * Verifies the reCAPTCHA token with Google reCAPTCHA Enterprise API.
     * @param token The token received from the frontend.
     * @param action The expected action (e.g., "LOGIN").
     * @return true if the assessment is successful and the score is high enough.
     */
    public static boolean verifyToken(String token, String action) {
        if (API_KEY == null || token == null || token.isEmpty()) {
            return false;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();

            // Create the request body based on your request.json structure
            String jsonBody = String.format(
                "{\"event\": {\"token\": \"%s\", \"expectedAction\": \"%s\", \"siteKey\": \"%s\"}}",
                token, action, SITE_KEY
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                System.out.println("[RecaptchaService] Assessment response: " + body);
                
                // Better score check: Look for scores that indicate a human (e.g., 0.6, 0.7, 0.8, 0.9, 1.0)
                // A very simple way without a full JSON parser:
                boolean isHuman = body.contains("\"score\": 0.6") || 
                                 body.contains("\"score\": 0.7") || 
                                 body.contains("\"score\": 0.8") || 
                                 body.contains("\"score\": 0.9") || 
                                 body.contains("\"score\": 1.0") ||
                                 body.contains("\"score\": 1");

                if (!isHuman) {
                    System.err.println("[RecaptchaService] Low score or bot detected. Response: " + body);
                }
                return isHuman;
            } else {
                System.err.println("[RecaptchaService] API Error: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
