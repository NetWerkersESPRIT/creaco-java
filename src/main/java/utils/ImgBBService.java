package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;
import java.net.URLEncoder;

public class ImgBBService {
    private static String API_KEY;
    private static final String API_URL = "https://api.imgbb.com/1/upload";

    static {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            API_KEY = properties.getProperty("IMGBB_API_KEY");
        } catch (IOException e) {
            System.err.println("Error loading IMGBB_API_KEY from config.properties: " + e.getMessage());
        }
    }

    public static String uploadImage(File imageFile) throws IOException, InterruptedException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new IOException("ImgBB API Key is missing!");
        }

        byte[] fileContent = java.nio.file.Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);

        HttpClient client = HttpClient.newHttpClient();

        // Prepare the body as x-www-form-urlencoded
        String requestBody = "key=" + URLEncoder.encode(API_KEY, StandardCharsets.UTF_8) +
                             "&image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String body = response.body();
            // Simple regex to extract the "url" from JSON
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"url\":\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                return matcher.group(1).replace("\\/", "/");
            }
            throw new IOException("Failed to parse ImgBB response: " + body);
        } else {
            throw new IOException("ImgBB API Error: " + response.statusCode() + " - " + response.body());
        }
    }
}
