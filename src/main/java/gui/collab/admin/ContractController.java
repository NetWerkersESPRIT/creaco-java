package gui.collab.admin;

import entities.Contract;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import services.ContractService;
import java.sql.SQLException;

public class ContractController {

    @FXML private ListView<Contract> contractsListView;

    private final ContractService contractService = new ContractService();
    private ObservableList<Contract> contractsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupListView();
        loadAllContracts();
    }

    private void setupListView() {
        contractsListView.setCellFactory(param -> new ListCell<Contract>() {
            @Override
            protected void updateItem(Contract item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cell = new VBox(5);
                    Label title = new Label(item.getTitle() + " (" + item.getContractNumber() + ")");
                    title.setStyle("-fx-font-weight: bold;");
                    Label details = new Label("Status: " + item.getStatus() + " | Amount: " + item.getAmount() + " DT");
                    cell.getChildren().addAll(title, details);
                    setGraphic(cell);
                }
            }
        });
        contractsListView.setItems(contractsList);

        contractsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && contractsListView.getSelectionModel().getSelectedItem() != null) {
                showDetail(contractsListView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void loadAllContracts() {
        try {
            contractsList.setAll(contractService.afficher());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showDetail(Contract c) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contract Details (Read-Only)");
        alert.setHeaderText(c.getTitle());
        alert.setContentText("Contract Number: " + c.getContractNumber() + "\n" +
                "Status: " + c.getStatus() + "\n" +
                "Amount: " + c.getAmount() + " DT\n" +
                "Dates: " + c.getStartDate() + " to " + c.getEndDate() + "\n\n" +
                "Terms: " + c.getTerms());
        alert.showAndWait();
    }
}
