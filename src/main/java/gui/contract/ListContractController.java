package gui.contract;

import entities.Contract;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ContractService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ListContractController {

    @FXML private TableView<Contract> contractTable;
    @FXML private TableColumn<Contract, Integer> idColumn;
    @FXML private TableColumn<Contract, String> contractNumberColumn;
    @FXML private TableColumn<Contract, String> titleColumn;
    @FXML private TableColumn<Contract, BigDecimal> amountColumn;
    @FXML private TableColumn<Contract, String> statusColumn;

    private final ContractService contractService = new ContractService();
    private final ObservableList<Contract> contractList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        contractNumberColumn.setCellValueFactory(new PropertyValueFactory<>("contractNumber"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        refreshTable();
    }

    public void refreshTable() {
        try {
            contractList.setAll(contractService.getAll());
            contractTable.setItems(contractList);
        } catch (SQLException e) {
            showAlert("Error", "Could not fetch contracts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAdd() {
        loadWindow("/contract/AddContract.fxml", "Add Contract");
    }

    @FXML
    private void handleEdit() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a contract to edit.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/contract/EditContract.fxml"));
            Parent root = loader.load();
            
            EditContractController controller = loader.getController();
            controller.setContract(selected);
            controller.setListController(this);

            Stage stage = new Stage();
            stage.setTitle("Edit Contract");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load edit screen.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Contract selected = contractTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a contract to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete contract " + selected.getContractNumber() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                contractService.delete(selected.getId());
                showAlert("Success", "Contract deleted successfully.", Alert.AlertType.INFORMATION);
                refreshTable();
            } catch (SQLException e) {
                showAlert("Error", "Could not delete contract: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof AddContractController) {
                ((AddContractController) controller).setListController(this);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load screen: " + fxml, Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
