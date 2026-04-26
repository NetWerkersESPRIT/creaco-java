package gui.post;

import entities.Post;
import entities.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.forum.PostService;
import services.UserService;
import services.forum.SpamDetectionService;
import services.NotificationService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PostModerationController {

    @FXML private VBox moderationList;
    @FXML private VBox listViewContainer;
    @FXML private VBox detailViewContainer;

    @FXML private Label detailTitleLabel;
    @FXML private Label detailAuthorLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailContentLabel;

    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;
    @FXML private Label pinRequestLabel;

    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final SpamDetectionService spamService = new SpamDetectionService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter metaFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, h:mm a");

    private Post selectedPost;

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Post Moderation", "Pages / Forum / Moderation");
        loadPendingPosts();

        // Populate User Profile
        entities.Users user = utils.SessionManager.getInstance().getCurrentUser();
        if (user != null && lblUsername != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            lblUsername.setText(displayName);

            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            lblUserRole.setText(role);

            // Special styling for ADMIN
            if ("ADMIN".equals(role)) {
                lblUserRole.setStyle("-fx-background-color: #434a75;");
            }
        }
    }

    private void loadPendingPosts() {
        moderationList.getChildren().clear();
        try {
            List<Post> posts = postService.getPendingPosts();
            if (posts.isEmpty()) {
                Label emptyLabel = new Label("NO PENDING POSTS.");
                emptyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #cbd5e1; -fx-padding: 40 0 40 0;");
                HBox centered = new HBox(emptyLabel);
                centered.setAlignment(javafx.geometry.Pos.CENTER);
                moderationList.getChildren().add(centered);
            } else {
                for (Post post : posts) {
                    moderationList.getChildren().add(buildModerationRow(post));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox buildModerationRow(Post post) {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 20 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Title
        Label titleLabel = new Label(post.getTitle());
        titleLabel.setPrefWidth(220);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 15px;");

        HBox titleBox = new HBox(8);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleBox.setPrefWidth(300);
        titleBox.getChildren().add(titleLabel);

        boolean isNew = post.getCreatedAt() != null && post.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(2));
        if (isNew) {
            Label newBadge = new Label("✨ NEW");
            newBadge.setStyle("-fx-background-color: #f43f5e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 10;");
            titleBox.getChildren().add(newBadge);
        }

        if (postService.isPinRequested(post.getId())) {
            Label pinBadge = new Label("📌 PIN REQ");
            pinBadge.setStyle("-fx-background-color: #ce2d7c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 10;");
            titleBox.getChildren().add(pinBadge);
        }

        // Author
        Users user = userService.getUserById(post.getUserId());
        Label authorLabel = new Label(user != null ? user.getUsername() : "Unknown");
        authorLabel.setPrefWidth(150);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-font-size: 14px;");

        // Spam Score with dynamic styling (Badge/Pill style)
        int spamScore = post.getSpamScore();
        Label spamLabel = new Label(spamScore + "/100");
        spamLabel.setPrefWidth(100);
        spamLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        String spamStyle = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 15; -fx-border-radius: 15; ";
        if (spamScore >= 80) {
            spamStyle += "-fx-text-fill: #991b1b; -fx-background-color: #fef2f2; -fx-border-color: #fecdd3; -fx-border-width: 1;"; // High Spam - Red
        } else if (spamScore >= 40) {
            spamStyle += "-fx-text-fill: #9a3412; -fx-background-color: #fff7ed; -fx-border-color: #fed7aa; -fx-border-width: 1;"; // Suspicious - Orange
        } else if (spamScore > 0) {
            spamStyle += "-fx-text-fill: #1e40af; -fx-background-color: #eff6ff; -fx-border-color: #bfdbfe; -fx-border-width: 1;"; // Minimal - Blue
        } else {
            spamStyle += "-fx-text-fill: #166534; -fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1;"; // Clean - Green
        }
        spamLabel.setStyle(spamStyle);

        // Date
        String dateStr = (post.getCreatedAt() != null) ? post.getCreatedAt().format(formatter) : "-";
        Label dateLabel = new Label(dateStr);
        dateLabel.setPrefWidth(200);
        dateLabel.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // View Button
        Button viewBtn = new Button("👁 VIEW");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #ce2d7c, #9124b8); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10 25;");
        viewBtn.setOnAction(e -> showDetail(post));

        HBox btnContainer = new HBox(viewBtn);
        btnContainer.setPrefWidth(150);
        btnContainer.setAlignment(javafx.geometry.Pos.CENTER);

        row.getChildren().addAll(titleBox, authorLabel, spamLabel, dateLabel, spacer, btnContainer);

        String defaultBg = isNew ? "#fff1f2" : "white";
        String hoverBg = isNew ? "#ffe4e6" : "#f8fafc";
        String borderColor = isNew ? "#fecdd3" : "#f1f5f9";

        row.setStyle("-fx-padding: 20 15; -fx-background-color: " + defaultBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 20 15; -fx-background-color: " + hoverBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 20 15; -fx-background-color: " + defaultBg + "; -fx-border-color: " + borderColor + "; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    private void showDetail(Post post) {
        this.selectedPost = post;
        detailTitleLabel.setText(post.getTitle());

        Users user = userService.getUserById(post.getUserId());
        detailAuthorLabel.setText((user != null) ? user.getUsername().toUpperCase() : "UNKNOWN");

        String date = (post.getCreatedAt() != null) ? post.getCreatedAt().format(metaFormatter).toUpperCase() : "-";
        detailDateLabel.setText(date);

        detailStatusLabel.setText("PENDING");
        detailContentLabel.setText(post.getContent());

        boolean hasPinReq = postService.isPinRequested(post.getId());
        if (pinRequestLabel != null) {
            pinRequestLabel.setVisible(hasPinReq);
            pinRequestLabel.setManaged(hasPinReq);
        }

        listViewContainer.setVisible(false);
        detailViewContainer.setVisible(true);
    }

    @FXML
    private void showList() {
        listViewContainer.setVisible(true);
        detailViewContainer.setVisible(false);
        loadPendingPosts();
    }

    @FXML
    private void onRunSpamFilter() {
        // This method re-analyzes all pending posts and sorts those with highest spam scores to the top
        loadPendingPosts();
        // Visual feedback
        gui.util.AlertHelper.showCustomAlert("Spam Analysis", "Spam scores have been updated and highlighted.", gui.util.AlertHelper.AlertType.INFORMATION);
    }

    @FXML
    private void onShowAnalytics() {
        try {
            javafx.scene.layout.StackPane contentArea =
                    (javafx.scene.layout.StackPane) moderationList.getScene().lookup("#contentArea");
            if (contentArea != null) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/post/forumStats.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleApprove() {
        if (selectedPost == null) return;

        boolean hasPinReq = postService.isPinRequested(selectedPost.getId());

        if (hasPinReq) {
            boolean confirm = gui.util.AlertHelper.showCustomAlert("Pin Request", "This user requested to pin this post. Do you want to pin it?", gui.util.AlertHelper.AlertType.CONFIRMATION);
            try {
                if (confirm) {
                    postService.acceptPinRequest(selectedPost.getId());
                } else {
                    postService.rejectPinRequest(selectedPost.getId());
                }
                selectedPost.setStatus("ACCEPTED");
                postService.updatePostStatus(selectedPost);
                
                // Notify User
                new NotificationService().notifyPostApproved(selectedPost.getUserId(), selectedPost.getId());
                
                gui.util.AlertHelper.showCustomAlert("Success", "Post approved!", gui.util.AlertHelper.AlertType.INFORMATION);
                showList();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            if (gui.util.AlertHelper.showCustomAlert("Approve?", "Make this post public?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    selectedPost.setStatus("ACCEPTED");
                    postService.updatePostStatus(selectedPost);
                    
                    // Notify User
                    new NotificationService().notifyPostApproved(selectedPost.getUserId(), selectedPost.getId());
                    
                    gui.util.AlertHelper.showCustomAlert("Success", "Post approved!", gui.util.AlertHelper.AlertType.INFORMATION);
                    showList();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @FXML
    private void handleRefuse() {
        if (selectedPost == null) return;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Refuse Post");
        dialog.setHeaderText(null);

        // Custom buttons
        ButtonType refuseButtonType = new ButtonType("REFUSE POST", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(refuseButtonType, ButtonType.CANCEL);

        VBox container = new VBox(15);
        container.setPadding(new javafx.geometry.Insets(25));
        container.setPrefWidth(450);
        container.setStyle("-fx-background-color: white;");

        Label label = new Label("REASON FOR REFUSAL");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 13px; -fx-letter-spacing: 1;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter the cause of rejection...");
        reasonArea.setPrefHeight(120);
        reasonArea.setWrapText(true);
        reasonArea.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8; -fx-font-size: 14px;");

        container.getChildren().addAll(label, reasonArea);
        dialog.getDialogPane().setContent(container);

        // Styling dialog buttons
        Button refuseBtn = (Button) dialog.getDialogPane().lookupButton(refuseButtonType);
        refuseBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == refuseButtonType) return reasonArea.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(reason -> {
            if (reason == null || reason.trim().isEmpty()) {
                gui.util.AlertHelper.showCustomAlert("Error", "Please provide a reason.", gui.util.AlertHelper.AlertType.ERROR);
                return;
            }
            try {
                selectedPost.setStatus("REJECTED");
                selectedPost.setRefusalReason(reason);
                postService.updatePostStatus(selectedPost);
                
                // Notify User
                new NotificationService().notifyPostRefused(selectedPost.getUserId(), selectedPost.getId());
                
                gui.util.AlertHelper.showCustomAlert("Refused", "Post has been rejected.", gui.util.AlertHelper.AlertType.INFORMATION);
                showList();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try {
            javafx.scene.layout.StackPane contentArea =
                    (javafx.scene.layout.StackPane) moderationList.getScene().lookup("#contentArea");
            if (contentArea != null) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/Users/Profile.fxml"));
                contentArea.getChildren().setAll((javafx.scene.Node) loader.load());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void logout(ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}