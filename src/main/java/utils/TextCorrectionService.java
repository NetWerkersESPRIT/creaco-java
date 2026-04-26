package utils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextCorrectionService {
    private static final String API_KEY = "WmfefzaQRx82LMbW";
    private static final String API_URL = "https://api.textgears.com/correct";

    /**
     * Corrects spelling and grammar errors in the provided text using TextGears API.
     * @param text The text to correct.
     * @return The corrected text, or the original text if correction fails.
     */
    public static String correctText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = API_URL + "?text=" + encodedText + "&key=" + API_KEY + "&language=en-US";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // Parse "corrected": "..." from JSON response
                Pattern pattern = Pattern.compile("\"corrected\":\\s*\"(.*?)\"");
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    String result = matcher.group(1);
                    // Basic unescaping for common characters
                    return result.replace("\\\"", "\"")
                                 .replace("\\\\", "\\")
                                 .replace("\\n", "\n")
                                 .replace("\\t", "\t");
                }
            }
        } catch (Exception e) {
            System.err.println("Text correction failed: " + e.getMessage());
        }
        return text; // Fallback to original text
    }
}
