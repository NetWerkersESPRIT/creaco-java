package utils;

import java.io.InputStream;
import java.util.Properties;


public class DocuSignConfig {

    public static final String AUTH_BASE_URL;
    public static final String API_BASE_URL;
    public static final String INTEGRATION_KEY;
    public static final String USER_ID;
    public static final String ACCOUNT_ID;
    public static final String PRIVATE_KEY_PATH;

    static {
        String authBase  = "https://account-d.docusign.com";
        String apiBase   = "https://demo.docusign.net/restapi";
        String intKey    = "";
        String userId    = "";
        String accountId = "";
        String keyPath   = "";

        try (InputStream in = DocuSignConfig.class
                .getClassLoader()
                .getResourceAsStream("docusign.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                authBase  = props.getProperty("docusign.auth.base.url",    authBase).trim();
                apiBase   = props.getProperty("docusign.api.base.url",     apiBase).trim();
                intKey    = props.getProperty("docusign.integration.key",  "").trim();
                userId    = props.getProperty("docusign.user.id",          "").trim();
                accountId = props.getProperty("docusign.account.id",       "").trim();
                keyPath   = props.getProperty("docusign.private.key.path", "").trim();
            } else {
                System.err.println("[DocuSignConfig] docusign.properties not found in classpath. " +
                        "Create src/main/resources/docusign.properties from the template.");
            }
        } catch (Exception e) {
            System.err.println("[DocuSignConfig] Failed to load docusign.properties: " + e.getMessage());
        }

        AUTH_BASE_URL    = authBase;
        API_BASE_URL     = apiBase;
        INTEGRATION_KEY  = intKey;
        USER_ID          = userId;
        ACCOUNT_ID       = accountId;
        PRIVATE_KEY_PATH = keyPath;
    }
}
