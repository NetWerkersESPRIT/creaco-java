package gui.collab.manager;

import entities.Contract;
import entities.Collaborator;
import entities.ContractAnalysisResult;
import entities.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import services.CollaboratorService;
import services.ContractService;
import services.UserService;
import utils.GroqContractAnalystService;

import java.text.SimpleDateFormat;

public class ContractConsultationController {

    // ── Existing FXML nodes ───────────────────────────────────────────────────
    @FXML private Label refLabel;
    @FXML private Label protocolLabel;
    @FXML private Label dateLabel;
    @FXML private Label partnerNameLabel;
    @FXML private Label partnerRepLabel;
    @FXML private Label creatorNameLabel;
    @FXML private Label statusLabel;
    @FXML private Button btnUpdate;
    @FXML private Button btnTransmit;

    // ── AI Panel FXML nodes ───────────────────────────────────────────────────
    @FXML private Button  btnAnalyze;
    @FXML private Label   aiStatusLabel;
    @FXML private VBox    aiResultsPane;

    @FXML private Label       overallScoreLabel;
    @FXML private Label       overallGradeLabel;

    @FXML private ProgressBar clarityBar;
    @FXML private Label       clarityScoreLabel;

    @FXML private ProgressBar budgetBar;
    @FXML private Label       budgetScoreLabel;

    @FXML private ProgressBar timelineBar;
    @FXML private Label       timelineScoreLabel;

    @FXML private VBox  flagsPane;
    @FXML private Label flagsLabel;

    // ── Services & state ─────────────────────────────────────────────────────
    private final ContractService     contractService  = new ContractService();
    private final CollaboratorService partnerService   = new CollaboratorService();
    private final UserService         userService      = new UserService();

    private Contract currentContract;
    private Runnable onBackRequested;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    // ── Public API ────────────────────────────────────────────────────────────

    public void setCallbacks(Runnable onBack) {
        this.onBackRequested = onBack;
    }

    public void setContract(Contract contract) {
        this.currentContract = contract;

        refLabel.setText("REF: " + contract.getContractNumber());
        protocolLabel.setText("DIGITAL PROTOCOL #" + contract.getContractNumber());
        dateLabel.setText(contract.getCreatedAt() != null
                ? df.format(contract.getCreatedAt())
                : df.format(new java.util.Date()));
        statusLabel.setText(contract.getStatus().toUpperCase());

        try {
            Collaborator partner = partnerService.getById(contract.getCollaboratorId());
            if (partner != null) {
                partnerNameLabel.setText(partner.getCompanyName());
                partnerRepLabel.setText("Representative: " + partner.getName());
            }
            Users creator = userService.getUserById(contract.getCreatorId());
            if (creator != null) creatorNameLabel.setText(creator.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lock action buttons if the contract is no longer a draft
        if (!"DRAFT".equalsIgnoreCase(contract.getStatus())) {
            btnTransmit.setVisible(false);
            btnUpdate.setDisable(true);
        }

        // Reset AI panel for the newly loaded contract
        resetAiPanel();
    }

    // ── Action handlers ───────────────────────────────────────────────────────

    @FXML
    private void onUpdateSpecifications() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Pending");
        alert.setHeaderText("Specification Editor");
        alert.setContentText("The interactive terms editor is currently under development.");
        alert.showAndWait();
    }

    @FXML
    private void onTransmitToPartner() {
        try {
            currentContract.setStatus("SENT_TO_PARTNER");
            currentContract.setSentAt(new java.util.Date());
            contractService.modifier(currentContract.getId(), currentContract);
            setContract(currentContract); // refresh UI

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Contract Transmitted");
            alert.setContentText("The digital protocol has been securely transmitted to the partner for signature.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack() {
        if (onBackRequested != null) onBackRequested.run();
    }

    // ── AI Analysis ───────────────────────────────────────────────────────────

    @FXML
    private void onRunAnalysis() {
        if (currentContract == null) return;

        // Update UI to loading state
        btnAnalyze.setDisable(true);
        btnAnalyze.setText("⏳  Analysing...");
        aiStatusLabel.setText("Sending contract data to AI analyst — this may take a few seconds...");
        aiStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7c3aed;");
        aiResultsPane.setVisible(false);
        aiResultsPane.setManaged(false);

        // Build string representations of dates and amount
        String startDateStr = currentContract.getStartDate() != null
                ? df.format(currentContract.getStartDate()) : "Not specified";
        String endDateStr   = currentContract.getEndDate()   != null
                ? df.format(currentContract.getEndDate())   : "Not specified";
        String amountStr    = currentContract.getAmount()    != null
                ? currentContract.getAmount().toPlainString() + " (currency unspecified)" : "Not specified";

        // Capture data before spawning the thread
        final String title          = currentContract.getTitle();
        final String terms          = currentContract.getTerms();
        final String paymentSched   = currentContract.getPaymentSchedule();
        final String confidClause   = currentContract.getConfidentialityClause();
        final String cancelTerms    = currentContract.getCancellationTerms();

        // Run API call off the JavaFX thread
        Thread analysisThread = new Thread(() -> {
            ContractAnalysisResult result = GroqContractAnalystService.analyse(
                    title, terms, amountStr, paymentSched,
                    startDateStr, endDateStr, confidClause, cancelTerms);

            // Always return to the FX thread to update UI
            Platform.runLater(() -> populateAiResults(result));
        });
        analysisThread.setDaemon(true);
        analysisThread.start();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Populates all AI panel nodes after the background analysis finishes. */
    private void populateAiResults(ContractAnalysisResult result) {
        btnAnalyze.setDisable(false);
        btnAnalyze.setText("▶  RUN ANALYSIS");

        if (!result.isSuccess()) {
            aiStatusLabel.setText("Analysis failed: " + result.getErrorMessage());
            aiStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
            return;
        }

        // ── Overall score ──────────────────────────────────────────────────────
        int overall = result.getOverallScore();
        overallScoreLabel.setText(overall + "%");
        overallGradeLabel.setText(ContractAnalysisResult.gradeLabel(overall)
                + " Acceptance Likelihood");

        // ── Sub-scores ─────────────────────────────────────────────────────────
        int clarity  = result.getClarityScore();
        int budget   = result.getBudgetRealismScore();
        int timeline = result.getTimelineFeasibilityScore();

        clarityBar.setProgress(result.getClarityProgress());
        clarityScoreLabel.setText(clarity + "/100");
        clarityScoreLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + ContractAnalysisResult.gradeColor(clarity) + ";");

        budgetBar.setProgress(result.getBudgetRealismProgress());
        budgetScoreLabel.setText(budget + "/100");
        budgetScoreLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + ContractAnalysisResult.gradeColor(budget) + ";");

        timelineBar.setProgress(result.getTimelineFeasibilityProgress());
        timelineScoreLabel.setText(timeline + "/100");
        timelineScoreLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: "
                + ContractAnalysisResult.gradeColor(timeline) + ";");

        // ── Flags ──────────────────────────────────────────────────────────────
        if (result.getFlags().isEmpty()) {
            flagsLabel.setText("✅  No issues detected.");
            flagsPane.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 8; " +
                    "-fx-padding: 10; -fx-border-color: #bbf7d0; -fx-border-radius: 8;");
            flagsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #166534;");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String flag : result.getFlags()) {
                sb.append("• ").append(flag).append("\n");
            }
            flagsLabel.setText(sb.toString().trim());
            flagsPane.setStyle("-fx-background-color: #fff7ed; -fx-background-radius: 8; " +
                    "-fx-padding: 10; -fx-border-color: #fed7aa; -fx-border-radius: 8;");
            flagsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #92400e;");
        }

        // ── Show results & update status label ────────────────────────────────
        aiResultsPane.setVisible(true);
        aiResultsPane.setManaged(true);
        aiStatusLabel.setText("Analysis complete — powered by Groq AI.");
        aiStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981;");
    }

    /** Resets the AI panel to its initial state (called when a new contract is loaded). */
    private void resetAiPanel() {
        aiResultsPane.setVisible(false);
        aiResultsPane.setManaged(false);
        aiStatusLabel.setText("Click to analyse this contract proposal.");
        aiStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        btnAnalyze.setDisable(false);
        btnAnalyze.setText("▶  RUN ANALYSIS");
    }
}
