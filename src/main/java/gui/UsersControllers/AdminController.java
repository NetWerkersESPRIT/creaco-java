package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AdminController {

    @FXML private TableView<Users> usersTable;
    @FXML private TableColumn<Users, Integer> colId;
    @FXML private TableColumn<Users, String>  colUsername, colEmail, colRole, colNumtel;
    @FXML private TableColumn<Users, Integer> colPoints;
    @FXML private TableColumn<Users, Void>    colActions;

    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() throws Exception {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colNumtel.setCellValueFactory(new PropertyValueFactory<>("numtel"));
        colPoints.setCellValueFactory(new PropertyValueFactory<>("points"));

        addActionButtons();
        loadUsers();
    }

    private void loadUsers() throws Exception {
        usersTable.setItems(FXCollections.observableArrayList(usersService.afficher()));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit   = new Button("✏ Edit");
            final Button btnDelete = new Button("🗑 Delete");

            {
                btnEdit.setOnAction(e -> {
                    Users u = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/EditUser.fxml"));
                        Stage stage = (Stage) getTableView().getScene().getWindow();
                        stage.setScene(new Scene(loader.load()));
                        // Pass user to EditUserController
                        EditUserController ctrl = loader.getController();
                        ctrl.setUser(u);
                    } catch (Exception ex) { ex.printStackTrace(); }
                });

                btnDelete.setOnAction(e -> {
                    Users u = getTableView().getItems().get(getIndex());
                    // Confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete User");
                    alert.setHeaderText("Are you sure?");
                    alert.setContentText("Delete user: " + u.getUsername() + "?");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                usersService.supprimer(u.getId());
                                loadUsers(); // refresh table
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Database Error");
                                errorAlert.setHeaderText("Delete Failed");
                                errorAlert.setContentText("Could not delete user: " + ex.getMessage());
                                errorAlert.showAndWait();
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    public void handleAddUser() throws Exception {
        // Try to find the dashboard's content area first
        javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) usersTable.getScene().lookup("#contentArea");
        
        if (contentArea != null) {
            // We are in the dashboard, load AddUser into the content area
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/AddUser.fxml"));
            javafx.scene.Node root = loader.load();
            
            // If the loaded node is a container with a center (like if it was still a BorderPane), 
            // but we converted it to StackPane, so we can just load it.
            contentArea.getChildren().setAll(root);
        } else {
            // Standalone mode
            Stage stage = (Stage) usersTable.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/Users/AddUser.fxml"))));
        }
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) usersTable.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }
}
