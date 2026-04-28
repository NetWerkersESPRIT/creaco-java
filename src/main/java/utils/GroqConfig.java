package utils;

import java.io.InputStream;
import java.util.Properties;

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
