package utils;

import entities.forum.SentimentResult;
import services.forum.AIAnalysisStrategy;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SentimentService implements AIAnalysisStrategy {
    private static final String API_KEY = "apv_8cd7e298-3314-484b-b173-18ad37ce89f5";
    private static final String API_URL = "https://api.apiverve.com/v1/sentimentanalysis";

    @Override
    public SentimentResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult("POSITIVE", 1.0);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            JSONObject bodyObj = new JSONObject();
            bodyObj.put("text", text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("x-api-key", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyObj.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("ApiVerve Response: " + response.body());
                JSONObject body = new JSONObject(response.body());
                if ("ok".equals(body.getString("status"))) {
                    JSONObject data = body.getJSONObject("data");
                    String sentiment = data.getString("sentimentText");
                    
                    double score = 0.0;
                    if (!data.isNull("normalizedScore")) {
                        score = Math.abs(data.getDouble("normalizedScore"));
                    } else if (!data.isNull("comparative")) {
                        score = Math.abs(data.getDouble("comparative"));
                    }
                    
                    System.out.println("Parsed Sentiment: " + sentiment + ", Score: " + score);
                    
                    // Convert ApiVerve labels to the app's expected labels
                    String label = "POSITIVE";
                    if (sentiment.toLowerCase().contains("negative")) {
                        label = "NEGATIVE";
                    } else if (sentiment.toLowerCase().contains("neutral")) {
                        label = "POSITIVE"; // Treat neutral as positive
                        score = 0.5; // Neutral is safe
                    }
                    
                    return new SentimentResult(label, score);
                }
            } else {
                System.err.println("ApiVerve Error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SentimentResult("POSITIVE", 1.0);
    }
}
