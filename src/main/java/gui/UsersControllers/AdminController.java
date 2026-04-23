package gui.UsersControllers;

import entities.Users;
import services.UsersService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AdminController {

    @FXML private VBox usersList;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;

    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() throws Exception {
        loadUsers();
        
        // Populate Navbar Profile
        entities.Users current = database.SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            String displayName = current.getUsername() != null ? current.getUsername() : "User";
            lblNavUsername.setText(displayName);
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
            if ("ADMIN".equals(role)) lblNavUserRole.setStyle("-fx-background-color: #434a75;");
        }
    }

    private void loadUsers() throws Exception {
        usersList.getChildren().clear();
        for (Users u : usersService.afficher()) {
            usersList.getChildren().add(buildUserRow(u));
        }
    }

    private javafx.scene.layout.HBox buildUserRow(Users u) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        
        // User Info (Avatar + Name + Email)
        javafx.scene.layout.HBox userInfo = new javafx.scene.layout.HBox(15);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userInfo.setPrefWidth(300);
        
        // Avatar Placeholder
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.setPrefSize(40, 40);
        avatar.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 10;");
        Label avatarLabel = new Label(String.valueOf(u.getUsername().charAt(0)).toUpperCase());
        avatarLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-pink;");
        avatar.getChildren().add(avatarLabel);
        
        javafx.scene.layout.VBox nameEmail = new javafx.scene.layout.VBox(2);
        Label nameLabel = new Label(u.getUsername());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 14px;");
        Label emailLabel = new Label(u.getEmail());
        emailLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
        nameEmail.getChildren().addAll(nameLabel, emailLabel);
        userInfo.getChildren().addAll(avatar, nameEmail);
        
        // Role Badge
        javafx.scene.layout.StackPane rolePane = new javafx.scene.layout.StackPane();
        rolePane.setPrefWidth(180);
        String roleText = u.getRole() != null ? u.getRole().replace("ROLE_", "") : "USER";
        Label roleBadge = new Label(roleText);
        roleBadge.setStyle("-fx-background-color: -fx-primary-gradient; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 12; -fx-background-radius: 8;");
        rolePane.getChildren().add(roleBadge);
        
        // Phone
        Label phoneLabel = new Label(u.getNumtel() != null && !u.getNumtel().isEmpty() ? u.getNumtel() : "N/A");
        phoneLabel.setPrefWidth(180);
        phoneLabel.setAlignment(javafx.geometry.Pos.CENTER);
        phoneLabel.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold;");
        
        // Actions
        javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10);
        actions.setPrefWidth(220);
        actions.setMinWidth(220);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button btnEdit = new Button("✎ EDIT");
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            System.out.println("Edit clicked for: " + u.getUsername());
            handleEdit(u);
        });
        
        Button btnDelete = new Button("🗑 DELETE");
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            System.out.println("Delete clicked for: " + u.getUsername());
            handleDelete(u);
        });
        
        actions.getChildren().addAll(btnEdit, btnDelete);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(userInfo, rolePane, phoneLabel, spacer, actions);
        
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        
        return row;
    }

    private void handleEdit(Users u) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/EditUser.fxml"));
            javafx.scene.Node root = loader.load();
            EditUserController ctrl = loader.getController();
            ctrl.setUser(u);
            
            javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) usersList.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleDelete(Users u) {
        System.out.println("Attempting to delete user ID: " + u.getId());
        if (gui.util.AlertHelper.showCustomAlert("Delete User?", "Are you sure you want to delete " + u.getUsername() + "?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                usersService.supprimer(u.getId());
                System.out.println("User deleted successfully from database.");
                loadUsers();
                gui.util.AlertHelper.showCustomAlert("Success", "User deleted successfully.", gui.util.AlertHelper.AlertType.INFORMATION);
            } catch (Exception ex) { 
                System.err.println("Delete failed: " + ex.getMessage());
                ex.printStackTrace();
                gui.util.AlertHelper.showCustomAlert("Error", "Could not delete user. This might be because the user has active posts or comments.\n\nDetails: " + ex.getMessage(), gui.util.AlertHelper.AlertType.ERROR);
            }
        }
    }


    @FXML
    public void handleAddUser() throws Exception {
        javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) usersList.getScene().lookup("#contentArea");
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/AddUser.fxml"));
            javafx.scene.Node root = loader.load();
            contentArea.getChildren().setAll(root);
        }
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) usersList.getScene().getWindow();
        stage.getScene().setRoot(FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml")));
        stage.setMaximized(true);
    }
}
