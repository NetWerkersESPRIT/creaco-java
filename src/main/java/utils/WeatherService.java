package utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    // Default coordinates for Tunis (can be made dynamic later)
    private static final String LAT = "36.819";
    private static final String LON = "10.1658";
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public static String getWeatherForDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        // Open-Meteo free tier typically provides 16 days of forecast
        if (date.isBefore(today)) {
            return "Weather info not available for past dates.";
        }
        if (date.isAfter(today.plusDays(15))) {
            return "Weather forecast only available up to 15 days in advance.";
        }

        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = String.format("%s?latitude=%s&longitude=%s&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code&timezone=auto&start_date=%s&end_date=%s",
                BASE_URL, LAT, LON, dateStr, dateStr);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseWeatherJson(response.body());
            } else {
                return "Weather service currently unavailable.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to fetch weather.";
        }
    }

    private static String parseWeatherJson(String json) {
        try {
            // Simple manual parsing for daily data
            double maxTemp = extractDouble(json, "temperature_2m_max");
            double minTemp = extractDouble(json, "temperature_2m_min");
            int rainProb = (int) extractDouble(json, "precipitation_probability_max");
            int weatherCode = (int) extractDouble(json, "weather_code");

            String icon = getWeatherIcon(weatherCode);
            return String.format("%s %.0f°C (☔ %d%%)", icon, maxTemp, rainProb);
        } catch (Exception e) {
            return "";
        }
    }

    private static double extractDouble(String json, String key) {
        String searchKey = "\"" + key + "\":[";
        int start = json.indexOf(searchKey);
        if (start == -1) return 0;
        start += searchKey.length();
        int end = json.indexOf("]", start);
        String val = json.substring(start, end);
        return Double.parseDouble(val.split(",")[0]);
    }

    private static String getWeatherIcon(int code) {
        if (code == 0) return "☀️"; // Clear sky
        if (code <= 3) return "☁️";  // Partly cloudy
        if (code <= 48) return "🌫️"; // Fog
        if (code <= 67) return "🌧️"; // Rain
        if (code <= 77) return "❄️"; // Snow
        if (code <= 82) return "🌦️"; // Showers
        if (code <= 99) return "⛈️"; // Thunderstorm
        return "🌡️";
    }
}
