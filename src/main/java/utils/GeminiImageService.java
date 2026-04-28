package utils;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiImageService {
    private static String API_KEY;
    // Note: Adjust the model name if using a specific version like imagen-3.0-generate-001
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-001:predict";

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/docusign.properties")) {
            // Reusing the credential loading logic from existing services
            properties.load(fis);
            // Assuming the same API key is stored or can be added there
            API_KEY = properties.getProperty("GEMINI_API_KEY");
        } catch (IOException e) {
            // Fallback to environment variable
            API_KEY = System.getenv("GEMINI_API_KEY");
        }
    }

    public static Image generateHeroBackground(String contextPrompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Crafting the Imagen 3 request body
            String fullPrompt = "Cinematic, high-resolution wide photographic shot of " + contextPrompt + 
                                ". Full width, professional business aesthetic, realistic textures, edge to edge composition.";
            
            String jsonBody = String.format(
                "{\"instances\": [{\"prompt\": \"%s\"}], \"parameters\": {\"sampleCount\": 1, \"aspectRatio\": \"16:9\"}}",
                fullPrompt.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // Extract Base64 data from response (simplified parsing)
                String body = response.body();
                Pattern pattern = Pattern.compile("\"bytesBase64Encoded\":\\s*\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(body);
                
                if (matcher.find()) {
                    String base64Data = matcher.group(1);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    return new Image(new ByteArrayInputStream(imageBytes));
                }
            } else {
                System.err.println("Imagen API Error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to generate image: " + e.getMessage());
        }
        return null; // Fallback to CSS default if generation fails
    }
}
