package gui.collab.admin;

import entities.Collaborator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.CollaboratorService;
import java.sql.SQLException;

public class PartnerController {

    @FXML private ListView<Collaborator> partnersListView;

    private final CollaboratorService partnerService = new CollaboratorService();
    private ObservableList<Collaborator> partnersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupListView();
        loadAllPartners();
    }

    private void setupListView() {
        partnersListView.setCellFactory(param -> new ListCell<Collaborator>() {
            @Override
            protected void updateItem(Collaborator item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(5);
                    Label name = new Label(item.getName() + " (" + item.getCompanyName() + ")");
                    name.setStyle("-fx-font-weight: bold;");
                    Label details = new Label("Domain: " + item.getDomain() + " | Email: " + item.getEmail());
                    cell.getChildren().addAll(name, details);
                    setGraphic(cell);
                }
            }
        });
        partnersListView.setItems(partnersList);

        partnersListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && partnersListView.getSelectionModel().getSelectedItem() != null) {
                showDetail(partnersListView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadAllPartners() {
        try {
            partnersList.setAll(partnerService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDetail(Collaborator collab) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Partner Details (Read-Only)");
        alert.setHeaderText(collab.getName());
        alert.setContentText("Company: " + collab.getCompanyName() + "\n" +
                "Email: " + collab.getEmail() + "\n" +
                "Website: " + collab.getWebsite() + "\n" +
                "Domain: " + collab.getDomain() + "\n\n" +
                "Description: " + collab.getDescription());
        alert.showAndWait();
    }
}
