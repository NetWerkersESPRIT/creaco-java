package gui.collab.contract;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import services.DocuSignService;

/**
 * Polls DocuSign specifically for RECIPIENT status summary.
 * Returns: "PARTNER_SIGNED", "CREATOR_SIGNED", "BOTH_SIGNED", or "NONE"
 */
public class DocuSignPoller extends ScheduledService<String> {

    private final DocuSignService docuSignService = new DocuSignService();
    private String envelopeId;

    public DocuSignPoller(String envelopeId) {
        this.envelopeId = envelopeId;
        setPeriod(Duration.seconds(30));
    }

    @Override
    protected Task<String> createTask() {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                if (envelopeId == null) return "NONE";
                return docuSignService.getRecipientStatusSummary(envelopeId);
            }
        };
    }
}
