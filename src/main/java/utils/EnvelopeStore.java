package utils;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;


public class EnvelopeStore {

    private static final Path STORE_PATH = Paths.get(
            System.getProperty("user.home"), ".creaco", "docusign_envelopes.properties");

    /** Persists l envelope ID lel contract. */
    public static void save(int contractId, String envelopeId) {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            Properties props = loadAll();
            props.setProperty(String.valueOf(contractId), envelopeId);
            try (OutputStream out = Files.newOutputStream(STORE_PATH)) {
                props.store(out, "CreaCo – DocuSign envelope tracking (auto-generated)");
            }
        } catch (IOException e) {
            System.err.println("[EnvelopeStore] Failed to save envelope ID: " + e.getMessage());
        }
    }

    /**
     * trajaa l envelope ID for the given contract ID
     * or trajaa null if no envelope has been sent yet
     */
    public static String get(int contractId) {
        return loadAll().getProperty(String.valueOf(contractId));
    }

    private static Properties loadAll() {
        Properties props = new Properties();
        if (Files.exists(STORE_PATH)) {
            try (InputStream in = Files.newInputStream(STORE_PATH)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("[EnvelopeStore] Failed to read store: " + e.getMessage());
            }
        }
        return props;
    }
}
