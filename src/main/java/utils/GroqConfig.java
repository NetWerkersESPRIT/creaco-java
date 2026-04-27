package utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration for the Groq API.
 *
 * The API key is loaded at runtime from src/main/resources/groq.properties
 * which is listed in .gitignore and is NEVER committed to the repository.
 *
 * SETUP (first time / new machine):
 *   1. Copy  src/main/resources/groq.properties.template
 *          → src/main/resources/groq.properties
 *   2. Paste your key (from https://console.groq.com/keys) as:
 *        groq.api.key=gsk_xxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   That's it — both GroqAssistService and GroqContractAnalystService
 *   will pick up the value automatically.
 */
public class GroqConfig {

    public static final String API_KEY;
    public static final String API_URL   = "https://api.groq.com/openai/v1/chat/completions";
    public static final String ASSIST_MODEL  = "llama-3.3-70b-versatile";
    public static final String ANALYST_MODEL = "llama-3.3-70b-versatile";

    static {
        String key = "";
        try (InputStream in = GroqConfig.class
                .getClassLoader()
                .getResourceAsStream("groq.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                key = props.getProperty("groq.api.key", "").trim();
            } else {
                System.err.println("[GroqConfig] groq.properties not found in classpath. " +
                        "Create src/main/resources/groq.properties and add your key.");
            }
        } catch (Exception e) {
            System.err.println("[GroqConfig] Failed to load groq.properties: " + e.getMessage());
        }
        API_KEY = key;
    }
}
