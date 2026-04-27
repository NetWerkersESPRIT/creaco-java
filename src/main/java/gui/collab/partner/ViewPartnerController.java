package gui.collab.partner;

import entities.Collaborator;
import entities.CollabRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.CollabRequestService;
import java.sql.SQLException;
import java.util.List;

public class ViewPartnerController {

    @FXML private Label pCompanyLabel;
    @FXML private Label pDomainLabel;
    @FXML private Label pNameLabel;
    @FXML private Label pEmailLabel;
    @FXML private Label pPhoneLabel;
    @FXML private Label pWebsiteLabel;
    @FXML private Label pAddressLabel;

    @FXML private TableView<CollabRequest> requestsTable;
    @FXML private TableColumn<CollabRequest, String> colTitle;
    @FXML private TableColumn<CollabRequest, String> colStatus;
    @FXML private TableColumn<CollabRequest, String> colPeriod;

    private final CollabRequestService requestService = new CollabRequestService();
    private Collaborator currentPartner;
    private Runnable onEditRequested;
    private Runnable onDeleteRequested;
    private Runnable onNewRequestRequested;

    public void setCallbacks(Runnable edit, Runnable delete, Runnable newReq) {
        this.onEditRequested = edit;
        this.onDeleteRequested = delete;
        this.onNewRequestRequested = newReq;
    }

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell for Period (Start Date - End Date)
        colPeriod.setCellFactory(column -> new TableCell<CollabRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    CollabRequest req = getTableRow().getItem();
                    String start = req.getStartDate() != null ? req.getStartDate().toString() : "?";
                    String end = req.getEndDate() != null ? req.getEndDate().toString() : "?";
                    setText(start + " - " + end);
                }
            }
        });

        // Status styling
        colStatus.setCellFactory(column -> new TableCell<CollabRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PENDING".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    else if ("ACCEPTED".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    else if ("REJECTED".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });
    }

    public void setPartner(Collaborator partner) {
        this.currentPartner = partner;
        pCompanyLabel.setText(partner.getCompanyName());
        pDomainLabel.setText(partner.getDomain());
        pNameLabel.setText(partner.getName());
        pEmailLabel.setText(partner.getEmail());
        pPhoneLabel.setText(partner.getPhone());
        pWebsiteLabel.setText(partner.getWebsite());
        pAddressLabel.setText(partner.getAddress());

        loadRequests();
    }

    private void loadRequests() {
        try {
            List<CollabRequest> requests = requestService.afficherByCollaborator(currentPartner.getId());
            requestsTable.setItems(FXCollections.observableArrayList(requests));
        } catch (SQLException e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }
    }

    @FXML private void onEdit() { if (onEditRequested != null) onEditRequested.run(); }
    @FXML private void onDelete() { if (onDeleteRequested != null) onDeleteRequested.run(); }
    @FXML private void onNewRequest() { if (onNewRequestRequested != null) onNewRequestRequested.run(); }
}
