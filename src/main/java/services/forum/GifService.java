package services.forum;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GifService using KLIPY API.
 * Uses the path-based app_key structure as per latest documentation.
 */
public class GifService {
    
    private static final String APP_KEY;
    static {
        java.util.Properties properties = new java.util.Properties();
        String key = "y4LavDL6QqUDpgS7TaDj7ws7EkODDSMocfnsNhOBPjWdkqoTTW3NRaechtCTWCyE"; // Fallback
        try (java.io.FileInputStream fis = new java.io.FileInputStream("config.properties")) {
            properties.load(fis);
            String propKey = properties.getProperty("KLIPY_API_KEY");
            if (propKey != null && !propKey.trim().isEmpty()) {
                key = propKey;
            }
        } catch (java.io.IOException e) {
            System.err.println("Error loading config.properties in GifService: " + e.getMessage());
        }
        APP_KEY = key;
    }

    private static final String BASE_URL = "https://api.klipy.com/api/v1/" + APP_KEY;
    private final HttpClient client;

    public GifService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Searches for GIFs using KLIPY.
     */
    public CompletableFuture<List<String>> searchGifs(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/gifs/search?q=" + encodedQuery + "&per_page=21";
        
        System.out.println("📡 KLIPY Request: Searching for '" + query + "'");
        return fetchGifs(url);
    }

    /**
     * Fetches trending GIFs using KLIPY.
     */
    public CompletableFuture<List<String>> getTrendingGifs() {
        String url = BASE_URL + "/gifs/trending?per_page=21";
        
        System.out.println("📡 KLIPY Request: Fetching Trending GIFs");
        return fetchGifs(url);
    }

    private CompletableFuture<List<String>> fetchGifs(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    List<String> urls = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        try {
                            System.out.println("📡 KLIPY Response: " + response.body());
                            JSONObject json = new JSONObject(response.body());
                            
                            // Klipy response variations: 
                            // 1. {"data": {"data": [...]}}
                            // 2. {"data": {"results": [...]}}
                            // 3. {"data": [...]}
                            JSONArray items = null;
                            JSONObject dataObj = json.optJSONObject("data");
                            if (dataObj != null) {
                                items = dataObj.optJSONArray("data");
                                if (items == null) items = dataObj.optJSONArray("results");
                            }
                            
                            if (items == null) items = json.optJSONArray("data");
                            if (items == null) items = json.optJSONArray("results");
                            
                            if (items != null) {
                                System.out.println("📡 KLIPY: Found " + items.length() + " items in response");
                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    String gifUrl = parseItem(item);
                                    if (gifUrl != null) {
                                        urls.add(gifUrl);
                                    }
                                }
                                System.out.println("📡 KLIPY: Successfully parsed " + urls.size() + " GIF URLs");
                            } else {
                                System.out.println("⚠️ KLIPY: No items found in response (items is null)");
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error parsing KLIPY response: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("❌ KLIPY API Error: " + response.statusCode() + " - " + response.body());
                    }
                    return urls;
                })
                .exceptionally(ex -> {
                    System.err.println("❌ KLIPY Connection Failed: " + ex.getMessage());
                    return new ArrayList<>();
                });
    }

    /**
     * Parses a single item to find a valid GIF URL.
     * Supports Klipy, Tenor, and Giphy response structures.
     */
    private String parseItem(JSONObject item) {
        // 1. Klipy structure: file -> md -> gif -> url
        JSONObject file = item.optJSONObject("file");
        if (file != null) {
            JSONObject md = file.optJSONObject("md");
            if (md == null) md = file.optJSONObject("sm");
            if (md == null) md = file.optJSONObject("hd");
            if (md != null) {
                JSONObject gif = md.optJSONObject("gif");
                if (gif != null && gif.has("url")) return gif.getString("url");
            }
        }

        // 2. Giphy-like structure (images -> fixed_width -> url)
        JSONObject images = item.optJSONObject("images");
        if (images != null) {
            JSONObject media = images.optJSONObject("fixed_width");
            if (media == null) media = images.optJSONObject("preview");
            if (media == null) media = images.optJSONObject("original");
            if (media != null && media.has("url")) return media.getString("url");
        }

        // 3. Tenor-like structure (media_formats -> tinygif -> url)
        JSONObject mediaFormats = item.optJSONObject("media_formats");
        if (mediaFormats != null) {
            JSONObject media = mediaFormats.optJSONObject("tinygif");
            if (media == null) media = mediaFormats.optJSONObject("gif");
            if (media != null && media.has("url")) return media.getString("url");
        }

        // 4. Fallback: Check for 'url' at root
        if (item.has("url") && item.getString("url").contains(".gif")) {
            return item.getString("url");
        }

        return null;
    }
}
