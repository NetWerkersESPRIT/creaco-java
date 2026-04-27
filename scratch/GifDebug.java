package scratch;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GifDebug {
    public static void main(String[] args) {
        String apiKey = "y4LavDL6QqUDpgS7TaDj7ws7EkODDSMocfnsNhOBPjWdkqoTTW3NRaechtCTWCyE";
        String url = "https://api.klipy.com/v1/gifs/search?api_key=" + apiKey + "&q=cat&limit=5";
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
