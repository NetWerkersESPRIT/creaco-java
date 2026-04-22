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

    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter metaFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy, h:mm a");

    private Post selectedPost;

    @FXML
    public void initialize() {
        loadPendingPosts();
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
        titleLabel.setPrefWidth(300);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748; -fx-font-size: 15px;");
        
        // Author
        Users user = userService.getUserById(post.getUserId());
        Label authorLabel = new Label(user != null ? user.getUsername() : "Unknown");
        authorLabel.setPrefWidth(150);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568; -fx-font-size: 14px;");
        
        // Spam Score
        Label spamLabel = new Label(post.getSpamScore() + "/100");
        spamLabel.setPrefWidth(120);
        spamLabel.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold;");
        
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

        row.getChildren().addAll(titleLabel, authorLabel, spamLabel, dateLabel, spacer, btnContainer);
        
        row.setOnMouseEntered(e -> row.setStyle("-fx-padding: 20 15; -fx-background-color: #f8fafc; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-padding: 20 15; -fx-background-color: white; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));
        
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
    private void handleApprove() {
        if (selectedPost == null) return;
        
        if (gui.util.AlertHelper.showCustomAlert("Approve?", "Make this post public?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try {
                selectedPost.setStatus("ACCEPTED");
                postService.updatePostStatus(selectedPost);
                gui.util.AlertHelper.showCustomAlert("Success", "Post approved!", gui.util.AlertHelper.AlertType.INFORMATION);
                showList();
            } catch (SQLException e) { e.printStackTrace(); }
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
                gui.util.AlertHelper.showCustomAlert("Refused", "Post has been rejected.", gui.util.AlertHelper.AlertType.INFORMATION);
                showList();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    @FXML
    public void logout(ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
