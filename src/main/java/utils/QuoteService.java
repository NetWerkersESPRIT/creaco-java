package utils;

import org.json.JSONArray;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class QuoteService {
    private List<String> quotes = new ArrayList<>();

    public QuoteService() {
        loadQuotes();
    }

    private void loadQuotes() {
        try (InputStream is = getClass().getResourceAsStream("/quotes.json")) {
            if (is != null) {
                try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                    String json = scanner.useDelimiter("\\A").next();
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        quotes.add(array.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Fallback if file not found or empty
        if (quotes.isEmpty()) {
            quotes.add("You are awesome, don't forget it!");
            quotes.add("Every storm runs out of rain.");
            quotes.add("You are stronger than you think.");
        }
    }

    public String getRandomQuote() {
        if (quotes.isEmpty()) return "Stay positive!";
        return quotes.get(new Random().nextInt(quotes.size()));
    }
}
