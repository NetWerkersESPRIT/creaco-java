package gui;

import entities.HelpTicket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.HelpTicketService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HelpDeskController {

    @FXML private VBox ticketsContainer;
    @FXML private VBox detailPanel;
    @FXML private Label detailSubject;
    @FXML private Label detailMeta;
    @FXML private Label detailStatus;
    @FXML private Label detailMessage;
    @FXML private TextArea responseArea;
    @FXML private StackPane statusBadge;

    private final HelpTicketService ticketService = new HelpTicketService();
    private HelpTicket selectedTicket;

    @FXML private Label totalTicketsLabel;
    @FXML private Label pendingTicketsLabel;
    @FXML private Label resolvedTicketsLabel;

    @FXML
    public void initialize() {
        refreshTickets();
        loadStats();
    }

    private void loadStats() {
        try {
            totalTicketsLabel.setText(String.valueOf(ticketService.getAllTickets().size()));
            pendingTicketsLabel.setText(String.valueOf(ticketService.getPendingTicketsCount()));
            resolvedTicketsLabel.setText(String.valueOf(ticketService.getAllTickets().stream().filter(t -> t.getStatus().equalsIgnoreCase("Resolved")).count()));
        } catch (SQLException ignored) {}
    }

    @FXML
    private void filterAll() {
        refreshTickets();
    }

    @FXML
    private void filterPending() {
        try {
            renderTickets(ticketService.getAllTickets().stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("Pending"))
                .toList());
        } catch (SQLException ignored) {}
    }

    @FXML
    private void filterResolved() {
        try {
            renderTickets(ticketService.getAllTickets().stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("Resolved"))
                .toList());
        } catch (SQLException ignored) {}
    }

    @FXML
    private void refreshTickets() {
        try {
            List<HelpTicket> tickets = ticketService.getAllTickets();
            renderTickets(tickets);
            loadStats();
        } catch (SQLException e) {
            AlertHelper.showError("Database Error", "Unable to load tickets: " + e.getMessage());
        }
    }

    private void renderTickets(List<HelpTicket> tickets) {
        ticketsContainer.getChildren().clear();
        for (HelpTicket t : tickets) {
            ticketsContainer.getChildren().add(createTicketRow(t));
        }
    }

    private Node createTicketRow(HelpTicket t) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(15));
        row.getStyleClass().add("sidebar-item"); 
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: #f1f5f9; -fx-border-width: 1;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Priority Icon
        StackPane iconBox = new StackPane();
        String iconColor = t.getPriority().equalsIgnoreCase("High") ? "#fee2e2" : "#fef3c7";
        String emoji = t.getPriority().equalsIgnoreCase("High") ? "🔴" : "🟡";
        iconBox.setStyle("-fx-background-color: " + iconColor + "; -fx-background-radius: 10; -fx-min-width: 40; -fx-min-height: 40;");
        iconBox.getChildren().add(new Label(emoji));

        VBox content = new VBox(5);
        Label subject = new Label(t.getSubject());
        subject.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        
        HBox meta = new HBox(10);
        Label creator = new Label(t.getCreatorName());
        creator.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label date = new Label(t.getCreatedAt().substring(0, 10));
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        
        meta.getChildren().addAll(creator, date);
        content.getChildren().addAll(subject, meta);
        
        row.getChildren().addAll(iconBox, content);

        row.setOnMouseClicked(e -> showTicketDetail(t));
        
        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: -fx-primary-pink; -fx-border-width: 1;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand; -fx-border-color: #f1f5f9; -fx-border-width: 1;"));

        return row;
    }

    private void showTicketDetail(HelpTicket t) {
        this.selectedTicket = t;
        detailPanel.setVisible(true);
        detailSubject.setText(t.getSubject());
        
        // Format date and time
        String formattedDate = "-";
        try {
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(t.getCreatedAt());
            formattedDate = ldt.format(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm"));
        } catch (Exception ignored) {}

        detailMeta.setText("By: " + t.getCreatorName() + " | Priority: " + t.getPriority() + " | Sent on: " + formattedDate);
        detailMessage.setText(t.getMessage());
        detailStatus.setText(t.getStatus());
        responseArea.setText(t.getAdminResponse() != null ? t.getAdminResponse() : "");

        String statusColor = t.getStatus().equalsIgnoreCase("Pending") ? "#64748b" : "#22c55e";
        statusBadge.setStyle("-fx-background-color: " + statusColor + "; -fx-padding: 5 15; -fx-background-radius: 15;");
    }

    @FXML
    private void onResolveTicket() {
        if (selectedTicket == null) return;
        try {
            ticketService.updateStatus(selectedTicket.getId(), "Resolved", responseArea.getText());
            AlertHelper.showInfo("Success", "Ticket resolved and response sent.");
            refreshTickets();
            detailPanel.setVisible(false);
        } catch (SQLException e) {
            AlertHelper.showError("Update Error", e.getMessage());
        }
    }

    @FXML
    private void onCloseTicket() {
        if (selectedTicket == null) return;
        try {
            ticketService.updateStatus(selectedTicket.getId(), "Closed", responseArea.getText());
            refreshTickets();
            detailPanel.setVisible(false);
        } catch (SQLException e) {
            AlertHelper.showError("Update Error", e.getMessage());
        }
    }

    @FXML
    private void onBackToDashboard() {
        loadScene("/gui/main-view.fxml");
    }

    @FXML
    private void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    private void loadScene(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ticketsContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
