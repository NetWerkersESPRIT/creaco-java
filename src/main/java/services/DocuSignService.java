package services;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth.OAuthToken;
import com.docusign.esign.model.*;
import entities.Contract;
import utils.DocuSignConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DocuSignService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy");

    public String sendEnvelopeForSignature(Contract contract,
                                           String partnerName, String partnerEmail,
                                           String creatorName, String creatorEmail)
            throws ApiException, IOException {

        ApiClient apiClient = buildAuthenticatedClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        EnvelopeDefinition envelope = buildEnvelope(contract, partnerName, partnerEmail, creatorName, creatorEmail);
        EnvelopeSummary summary = envelopesApi.createEnvelope(DocuSignConfig.ACCOUNT_ID, envelope);
        return summary.getEnvelopeId();
    }

    public String getEmbeddedSigningUrl(String envelopeId, String creatorName, String creatorEmail)
            throws ApiException, IOException {
        ApiClient apiClient = buildAuthenticatedClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        RecipientViewRequest viewRequest = new RecipientViewRequest();
        viewRequest.setReturnUrl("https://www.docusign.com"); // URL where user is redirected after signing
        viewRequest.setAuthenticationMethod("none");
        viewRequest.setEmail(creatorEmail);
        viewRequest.setUserName(creatorName);
        viewRequest.setClientUserId("1000"); // Must match the ID set during envelope creation

        ViewUrl viewUrl = envelopesApi.createRecipientView(DocuSignConfig.ACCOUNT_ID, envelopeId, viewRequest);
        return viewUrl.getUrl();
    }

    /**
     * Polls DocuSign for the current status of an envelope. to check individual signature progress.
     *
     * @return A string summary of progress: "PARTNER_SIGNED", "CREATOR_SIGNED", "BOTH_SIGNED", or "NONE"
     */
    public String getRecipientStatusSummary(String envelopeId) throws ApiException, IOException {
        ApiClient apiClient = buildAuthenticatedClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

        Recipients recipients = envelopesApi.listRecipients(DocuSignConfig.ACCOUNT_ID, envelopeId);

        boolean partnerSigned = false;
        boolean creatorSigned = false;

        for (Signer signer : recipients.getSigners()) {
            boolean isCompleted = "completed".equalsIgnoreCase(signer.getStatus());
            if ("1".equals(signer.getRecipientId())) partnerSigned = isCompleted;
            if ("2".equals(signer.getRecipientId())) creatorSigned = isCompleted;
        }

        if (partnerSigned && creatorSigned) return "BOTH_SIGNED";
        if (partnerSigned) return "PARTNER_SIGNED";
        if (creatorSigned) return "CREATOR_SIGNED";
        return "NONE";
    }

    public byte[] downloadCombinedDocument(String envelopeId) throws ApiException, IOException {
        ApiClient apiClient = buildAuthenticatedClient();
        EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
        
        // "combined" retrieves all documents in the envelope plus the summary/audit log
        return envelopesApi.getDocument(DocuSignConfig.ACCOUNT_ID, envelopeId, "combined");
    }

    private ApiClient buildAuthenticatedClient() throws ApiException, IOException {
        ApiClient apiClient = new ApiClient(DocuSignConfig.AUTH_BASE_URL);
        apiClient.setBasePath(DocuSignConfig.API_BASE_URL);

        List<String> scopes = Arrays.asList("signature", "impersonation");
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(DocuSignConfig.PRIVATE_KEY_PATH));

        OAuthToken token = apiClient.requestJWTUserToken(
                DocuSignConfig.INTEGRATION_KEY, DocuSignConfig.USER_ID,
                scopes, privateKeyBytes, 3600L);

        apiClient.setAccessToken(token.getAccessToken(), token.getExpiresIn());
        return apiClient;
    }

    private EnvelopeDefinition buildEnvelope(Contract contract,
                                              String partnerName, String partnerEmail,
                                              String creatorName, String creatorEmail) {

        String html = buildContractHtml(contract, partnerName, creatorName);
        String base64Doc = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));

        Document document = new Document();
        document.setDocumentBase64(base64Doc);
        document.setName("Collaboration Contract – " + contract.getContractNumber());
        document.setFileExtension("html");
        document.setDocumentId("1");

        // Recipient 1: Partner (Remote/Email)
        Signer partner = new Signer();
        partner.setEmail(partnerEmail);
        partner.setName(partnerName);
        partner.setRecipientId("1");
        partner.setRoutingOrder("1");

        SignHere ps = new SignHere();
        ps.setAnchorString("**PARTNER_SIGNATURE**");
        ps.setAnchorUnits("pixels");
        ps.setAnchorXOffset("20");
        ps.setAnchorYOffset("-5");
        Tabs pTabs = new Tabs();
        pTabs.setSignHereTabs(Arrays.asList(ps));
        partner.setTabs(pTabs);

        // Recipient 2: Creator (Embedded - NO EMAIL)
        Signer creator = new Signer();
        creator.setEmail(creatorEmail);
        creator.setName(creatorName);
        creator.setRecipientId("2");
        creator.setRoutingOrder("1");
        // Setting clientUserId makes this an embedded signer (DocuSign won't send an email)
        creator.setClientUserId("1000");

        SignHere cs = new SignHere();
        cs.setAnchorString("**CREATOR_SIGNATURE**");
        cs.setAnchorUnits("pixels");
        cs.setAnchorXOffset("20");
        cs.setAnchorYOffset("-5");
        Tabs cTabs = new Tabs();
        cTabs.setSignHereTabs(Arrays.asList(cs));
        creator.setTabs(cTabs);

        Recipients recipients = new Recipients();
        recipients.setSigners(Arrays.asList(partner, creator));

        EnvelopeDefinition envelopeDef = new EnvelopeDefinition();
        envelopeDef.setEmailSubject("Action Required: Please Sign – " + contract.getContractNumber());
        envelopeDef.setDocuments(Arrays.asList(document));
        envelopeDef.setRecipients(recipients);
        envelopeDef.setStatus("sent");

        return envelopeDef;
    }

    private String buildContractHtml(Contract contract, String partnerName, String creatorName) {
        String terms = esc(contract.getTerms());
        return "<html><body style='font-family:Arial;margin:50px;'>"
                + "<h1>Contract " + esc(contract.getContractNumber()) + "</h1>"
                + "<h2>Terms</h2><p>" + terms + "</p>"
                + "<br/><br/><br/>"
                + "<table width='100%'><tr>"
                + "<td><strong>**PARTNER_SIGNATURE**</strong><br/>Partner: " + esc(partnerName) + "</td>"
                + "<td><strong>**CREATOR_SIGNATURE**</strong><br/>Creator: " + esc(creatorName) + "</td>"
                + "</tr></table>"
                + "</body></html>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
