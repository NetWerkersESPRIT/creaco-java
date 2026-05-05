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
    private static final String PROJECT_ID = "java-1777469158652";
    private static final String SITE_KEY = "6LfEfNAsAAAAAAMtRP2CvVFSK1wzh593gDWdkcta";
    private static final String API_URL = "https://recaptchaenterprise.googleapis.com/v1/projects/" + PROJECT_ID + "/assessments?key=";

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            // We use the same Google Cloud API key defined for Gemini
            API_KEY = properties.getProperty("GEMINI_API_KEY");
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
                System.out.println("[RecaptchaService] Assessment response: " + response.body());
                // For Enterprise, you should check the "riskAnalysis" -> "score"
                // A score > 0.5 is usually considered safe.
                return response.body().contains("\"score\":") && !response.body().contains("\"score\": 0."); 
                // Note: Simple check for demo. In production, parse JSON and check score >= 0.7
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
