package scratch;

import services.forum.GifService;
import java.util.List;

public class GifTest {
    public static void main(String[] args) {
        GifService service = new GifService();
        System.out.println("Fetching trending GIFs...");
        service.getTrendingGifs().thenAccept(urls -> {
            System.out.println("Found " + urls.size() + " trending GIFs.");
            if (!urls.isEmpty()) {
                System.out.println("First GIF URL: " + urls.get(0));
            }
        }).join();

        System.out.println("\nSearching for 'cat' GIFs...");
        service.searchGifs("cat").thenAccept(urls -> {
            System.out.println("Found " + urls.size() + " 'cat' GIFs.");
            if (!urls.isEmpty()) {
                System.out.println("First 'cat' GIF URL: " + urls.get(0));
            }
        }).join();
    }
}
