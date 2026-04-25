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
    @FXML private javafx.scene.image.ImageView imgNavAvatar;
    @FXML private Label lblGeminiResponse;
    @FXML private VBox geminiResponseContainer;
    @FXML private Button btnAskGemini;
    @FXML private TextField txtGeminiQuery;

    private final UsersService usersService = new UsersService();

    @FXML
    public void initialize() throws Exception {
        loadUsers();
        
        // Populate Navbar Profile
        entities.Users current = utils.SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            String displayName = current.getUsername() != null ? current.getUsername() : "User";
            lblNavUsername.setText(displayName);
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
            if ("ADMIN".equals(role)) lblNavUserRole.setStyle("-fx-background-color: #434a75;");

            // Navbar Avatar
            if (imgNavAvatar != null) {
                String avatarUrl = current.getImage();
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + current.getUsername();
                }
                imgNavAvatar.setImage(new javafx.scene.image.Image(avatarUrl, true));
            }
        }
    }

    @FXML
    private void onAskGemini() {
        btnAskGemini.setDisable(true);
        btnAskGemini.setText("🤖 Thinking...");
        geminiResponseContainer.setVisible(true);
        geminiResponseContainer.setManaged(true);
        lblGeminiResponse.setText("Gemini is analyzing your application data...");

        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                // Collect statistics
                int userCount = usersService.afficher().size();
                int courseCount = new services.CourseService().afficher().size();
                int postCount = new services.forum.PostService().afficher().size();
                int commentCount = new services.forum.CommentService().afficher().size();
                int ideaCount = new services.IdeaService().afficher().size();
                int missionCount = new services.MissionService().afficher().size();

                String prompt = String.format(
                    "You are the AI Admin Assistant for the 'CreaCo' application. " +
                    "Here is the current status of the application:\\n" +
                    "- Total Users: %d\\n" +
                    "- Total Courses: %d\\n" +
                    "- Total Forum Posts: %d\\n" +
                    "- Total Comments: %d\\n" +
                    "- Total Innovation Ideas: %d\\n" +
                    "- Total Missions: %d\\n\\n" +
                    "Please provide a short, professional summary of the app's health and a small motivational tip for the admin. Use bullet points if needed.",
                    userCount, courseCount, postCount, commentCount, ideaCount, missionCount
                );

                return utils.GeminiService.getGeminiResponse(prompt);
            }
        };

        task.setOnSucceeded(e -> {
            lblGeminiResponse.setText(task.getValue());
            btnAskGemini.setDisable(false);
            btnAskGemini.setText("📊 Get Status Report");
        });

        task.setOnFailed(e -> {
            lblGeminiResponse.setText("❌ Error: " + task.getException().getMessage());
            btnAskGemini.setDisable(false);
            btnAskGemini.setText("📊 Get Status Report");
        });

        new Thread(task).start();
    }

    @FXML
    private void onGeminiDiscuss() {
        String query = txtGeminiQuery.getText().trim();
        if (query.isEmpty()) return;

        txtGeminiQuery.setDisable(true);
        geminiResponseContainer.setVisible(true);
        geminiResponseContainer.setManaged(true);
        lblGeminiResponse.setText("Gemini is typing...");

        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                // Collect statistics to provide context even for manual questions
                int userCount = usersService.afficher().size();
                int courseCount = new services.CourseService().afficher().size();
                int postCount = new services.forum.PostService().afficher().size();
                int commentCount = new services.forum.CommentService().afficher().size();
                int ideaCount = new services.IdeaService().afficher().size();
                int missionCount = new services.MissionService().afficher().size();

                String context = String.format(
                    "Context: Application 'CreaCo', Users: %d, Courses: %d, Posts: %d, Ideas: %d.\nQuestion: ",
                    userCount, courseCount, postCount, ideaCount
                );

                return utils.GeminiService.getGeminiResponse(context + query);
            }
        };

        task.setOnSucceeded(e -> {
            lblGeminiResponse.setText(task.getValue());
            txtGeminiQuery.setDisable(false);
            txtGeminiQuery.clear();
        });

        task.setOnFailed(e -> {
            lblGeminiResponse.setText("❌ Error: " + task.getException().getMessage());
            txtGeminiQuery.setDisable(false);
        });

        new Thread(task).start();
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
        // Use the same padding as the header (10 15 10 15) for perfect alignment
        row.setStyle("-fx-padding: 10 15 10 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");
        
        // User Info (Avatar + Name + Email) - Width: 300
        javafx.scene.layout.HBox userInfo = new javafx.scene.layout.HBox(15);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userInfo.setPrefWidth(300);
        
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.setPrefSize(40, 40);
        avatar.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 10;");
        
        javafx.scene.image.ImageView avatarView = new javafx.scene.image.ImageView();
        avatarView.setFitHeight(40);
        avatarView.setFitWidth(40);
        avatarView.setPreserveRatio(true);
        
        String avatarUrl = u.getImage();
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + u.getUsername();
        }
        avatarView.setImage(new javafx.scene.image.Image(avatarUrl, true));
        
        // Clip to rounded corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(40, 40);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        avatarView.setClip(clip);
        
        avatar.getChildren().add(avatarView);
        
        javafx.scene.layout.VBox nameEmail = new javafx.scene.layout.VBox(2);
        Label nameLabel = new Label(u.getUsername());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 14px;");
        Label emailLabel = new Label(u.getEmail());
        emailLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
        nameEmail.getChildren().addAll(nameLabel, emailLabel);
        userInfo.getChildren().addAll(avatar, nameEmail);
        
        // Role Badge - Width: 180 (Centered)
        javafx.scene.layout.StackPane rolePane = new javafx.scene.layout.StackPane();
        rolePane.setPrefWidth(180);
        String roleText = u.getRole() != null ? u.getRole().replace("ROLE_", "") : "USER";
        Label roleBadge = new Label(roleText);
        roleBadge.setStyle("-fx-background-color: -fx-primary-gradient; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 12; -fx-background-radius: 8;");
        rolePane.getChildren().add(roleBadge);
        
        // Phone - Width: 180 (Centered)
        Label phoneLabel = new Label(u.getNumtel() != null && !u.getNumtel().isEmpty() ? u.getNumtel() : "N/A");
        phoneLabel.setPrefWidth(180);
        phoneLabel.setAlignment(javafx.geometry.Pos.CENTER);
        phoneLabel.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold;");
        
        // Actions - Width: 200 (Matches Header)
        javafx.scene.layout.HBox actions = new javafx.scene.layout.HBox(10);
        actions.setPrefWidth(200);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button btnEdit = new Button("✎");
        btnEdit.setTooltip(new Tooltip("Edit User"));
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleEdit(u));
        
        Button btnBan = new Button(u.isBanned() ? "🔓" : "🚫");
        btnBan.setTooltip(new Tooltip(u.isBanned() ? "Unban User" : "Ban User"));
        String banColor = u.isBanned() ? "#10b981" : "#f59e0b";
        btnBan.setStyle("-fx-background-color: " + banColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
        btnBan.setOnAction(e -> handleToggleBan(u));

        Button btnDelete = new Button("🗑");
        btnDelete.setTooltip(new Tooltip("Delete User"));
        btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 8; -fx-padding: 8 12; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> handleDelete(u));
        
        actions.getChildren().addAll(btnEdit, btnBan, btnDelete);
        
        // Create spacers to match the header's Region hgrow="ALWAYS"
        javafx.scene.layout.Region spacer1 = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer1, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.Region spacer2 = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.Region spacer3 = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer3, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().addAll(userInfo, spacer1, rolePane, spacer2, phoneLabel, spacer3, actions);
        
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 10 15 10 15; -fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 10 15 10 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        
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


    private void handleToggleBan(Users u) {
        boolean newStatus = !u.isBanned();
        String actionText = newStatus ? "ban" : "unban";
        
        if (gui.util.AlertHelper.showCustomAlert(newStatus ? "Ban User?" : "Unban User?", 
                "Are you sure you want to " + actionText + " " + u.getUsername() + "?", 
                gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                usersService.modifierBan(u.getId(), newStatus);
                loadUsers(); // Refresh the list
                gui.util.AlertHelper.showCustomAlert("Success", "User " + actionText + "ned successfully.", 
                        gui.util.AlertHelper.AlertType.INFORMATION);
            } catch (Exception ex) {
                ex.printStackTrace();
                gui.util.AlertHelper.showCustomAlert("Error", "Could not " + actionText + " user: " + ex.getMessage(), 
                        gui.util.AlertHelper.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void handleAddUser() throws Exception {
        javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) usersList.getScene().lookup("#contentArea");
        if (contentArea != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/AddUser.fxml"));
            contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
        }
    }

    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try {
            javafx.scene.layout.StackPane contentArea =
                (javafx.scene.layout.StackPane) usersList.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            }
        } catch (Exception e) { e.printStackTrace(); }
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
