package services.forum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmojiService {

    private static final String BASE_URL = "https://emojihub.yurace.pro/api";
    private final HttpClient client;

    public EmojiService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static class Emoji {
        public String name;
        public String category;
        public String group;
        public String htmlCode;
        public String character; // Decoded emoji

        public Emoji(JSONObject json) {
            this.name = json.optString("name");
            this.category = json.optString("category");
            this.group = json.optString("group");
            JSONArray codes = json.optJSONArray("htmlCode");
            if (codes != null && codes.length() > 0) {
                this.htmlCode = codes.getString(0);
                this.character = decodeHtmlEntity(this.htmlCode);
            }
        }

        private String decodeHtmlEntity(String htmlCode) {
            try {
                // Handle decimal: &#128512; or hex: &#x1F600;
                int codePoint;
                if (htmlCode.contains("&#x")) {
                    String hex = htmlCode.replaceAll("[^a-fA-F0-9]", "");
                    codePoint = Integer.parseInt(hex, 16);
                } else {
                    String decimal = htmlCode.replaceAll("[^0-9]", "");
                    codePoint = Integer.parseInt(decimal);
                }
                return new String(Character.toChars(codePoint));
            } catch (Exception e) {
                System.err.println("❌ Failed to decode emoji: " + htmlCode);
                return "?";
            }
        }
    }

    public CompletableFuture<List<Emoji>> fetchAllEmojis() {
        return fetchFromUrl(BASE_URL + "/all");
    }

    public CompletableFuture<List<Emoji>> fetchByCategory(String category) {
        return fetchFromUrl(BASE_URL + "/all/category/" + category.replace(" ", "_"));
    }

    public CompletableFuture<List<Emoji>> fetchByGroup(String group) {
        return fetchFromUrl(BASE_URL + "/all/group/" + group.replace(" ", "_"));
    }

    private CompletableFuture<List<Emoji>> fetchFromUrl(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<Emoji> emojis = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        JSONArray array = new JSONArray(response.body());
                        for (int i = 0; i < array.length(); i++) {
                            emojis.add(new Emoji(array.getJSONObject(i)));
                        }
                    }
                    return emojis;
                })
                .exceptionally(ex -> {
                    System.err.println("❌ EmojiHub API Error: " + ex.getMessage());
                    return new ArrayList<>();
                });
    }
}
