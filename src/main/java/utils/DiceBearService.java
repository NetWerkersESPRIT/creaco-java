package utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility service to generate DiceBear avatar URLs.
 */
public class DiceBearService {
    private static final String BASE_URL = "https://api.dicebear.com/9.x/thumbs/png?seed=";

    /**
     * Generates a DiceBear avatar URL for the given seed.
     * @param seed The seed for the avatar (e.g., username or email).
     * @return A complete DiceBear API URL.
     */
    public static String generateAvatarUrl(String seed) {
        if (seed == null || seed.trim().isEmpty()) {
            seed = "default";
        }
        try {
            return BASE_URL + URLEncoder.encode(seed.trim(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            // Fallback if encoding fails
            return BASE_URL + seed.replaceAll("[^a-zA-Z0-9]", "");
        }
    }

    /**
     * Generates a DiceBear initials avatar URL for the given seed.
     * @param seed The seed for the initials (usually username).
     * @return A complete DiceBear API URL for initials.
     */
    public static String generateInitialsUrl(String seed) {
        if (seed == null || seed.trim().isEmpty()) {
            seed = "??";
        }
        // Take only first 2 letters if possible
        String initials = seed.trim();
        if (initials.length() > 2) {
            initials = initials.substring(0, 2);
        }
        try {
            return "https://api.dicebear.com/9.x/initials/png?seed=" + URLEncoder.encode(initials, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return "https://api.dicebear.com/9.x/initials/png?seed=" + initials;
        }
    }
}
