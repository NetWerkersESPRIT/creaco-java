package gui.post;

import entities.Post;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.forum.PostService;
import main.FxApplication;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.awt.Desktop;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BackofficeController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private VBox postsContainer;
    @FXML
    private Label statusLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox moderationView;
    @FXML
    private Button moderationNavButton;
    @FXML
    private Button forumNavButton;

    private final PostService postService = new PostService();

    private static final String NAV_ACTIVE   = "-fx-background-color: #f59e0b; -fx-text-fill: #1f365c; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;";
    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;";

    @FXML
    public void initialize() {
        FxApplication.setBackofficeController(this);
        setActiveNav(moderationNavButton);
        loadPendingPosts();
    }

    private void setActiveNav(Button active) {
        moderationNavButton.setStyle(NAV_INACTIVE);
        forumNavButton.setStyle(NAV_INACTIVE);
        active.setStyle(NAV_ACTIVE);
    }
    public void loadPendingPosts() {
        postsContainer.getChildren().clear();
        try {
            List<Post> pendingPosts = postService.getPendingPosts();

            if (pendingPosts.isEmpty()) {
                statusLabel.setText("No pending posts.");
                Label emptyLabel = new Label("✅  All posts have been moderated. No pending items.");
                emptyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b; -fx-padding: 24;");
                postsContainer.getChildren().add(emptyLabel);
                return;
            }

            statusLabel.setText(pendingPosts.size() + " post(s) pending review.");
            for (Post post : pendingPosts) {
                postsContainer.getChildren().add(buildPostCard(post));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: could not load pending posts.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load pending posts:\n" + e.getMessage());
        }
    }

    private VBox buildPostCard(Post post) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #dbe4f0;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // ── Header row ──────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // PENDING badge
        Label badge = new Label("⏳  PENDING");
        badge.setStyle(
                "-fx-background-color: #fef3c7;" +
                "-fx-text-fill: #92400e;" +
                "-fx-padding: 3 10 3 10;" +
                "-fx-background-radius: 8;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;"
        );

        Label titleLabel = new Label(safeText(post.getTitle()));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a5f;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        //data
        header.getChildren().addAll(badge, titleLabel);
        Label metaLabel = new Label("Posted: " + formatDate(post.getCreatedAt()));
        metaLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        //content
        Label contentLabel = new Label(safeText(post.getContent()));
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
        //seperator line
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: #e2e8f0;");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button acceptBtn = new Button("✅  Accept");
        acceptBtn.setStyle(
                "-fx-background-color: #dcfce7;" +
                "-fx-text-fill: #15803d;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 9 20 9 20;" +
                "-fx-cursor: hand;"
        );
        acceptBtn.setOnAction(e -> handleAccept(post, card));

        Button refuseBtn = new Button("❌  Refuse");
        refuseBtn.setStyle(
                "-fx-background-color: #fee2e2;" +
                "-fx-text-fill: #b91c1c;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 9 20 9 20;" +
                "-fx-cursor: hand;"
        );
        refuseBtn.setOnAction(e -> handleRefuse(post, card));

        actions.getChildren().addAll(acceptBtn, refuseBtn);

        card.getChildren().addAll(header, metaLabel, contentLabel);

        // --- MEDIA RENDERING (IMAGE) ---
        if (post.getImageName() != null && !post.getImageName().isEmpty()) {
            try {
                String imagePath = "src/main/resources/uploads/images/" + post.getImageName();
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(300);
                    imageView.setPreserveRatio(true);
                    
                    // Rounded corners for the image
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                    clip.setArcWidth(15);
                    clip.setArcHeight(15);
                    clip.widthProperty().bind(imageView.fitWidthProperty());
                    
                    // We can't bind height easily before loading, so we use a listener or just a reasonable default
                    image.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() == 1.0) {
                            clip.setHeight(imageView.getBoundsInLocal().getHeight());
                        }
                    });
                    clip.setHeight(200); 

                    imageView.setClip(clip);
                    card.getChildren().add(imageView);
                }
            } catch (Exception e) {
                System.err.println("Error loading image in backoffice: " + e.getMessage());
            }
        }

        // --- MEDIA RENDERING (PDF) ---
        if (post.getPdfName() != null && !post.getPdfName().isEmpty()) {
            Button openPdfBtn = new Button("📄 View Attachment (PDF)");
            openPdfBtn.setStyle(
                "-fx-background-color: #f1f5f9;" +
                "-fx-text-fill: #1e293b;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 8 16;" +
                "-fx-cursor: hand;"
            );
            openPdfBtn.setOnAction(e -> {
                try {
                    File file = new File("src/main/resources/uploads/pdfs/" + post.getPdfName());
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert(Alert.AlertType.WARNING, "File Not Found", "The PDF file could not be found locally.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not open the PDF file.");
                }
            });
            card.getChildren().add(openPdfBtn);
        }

        card.getChildren().addAll(separator, actions);
        return card;
    }

    //moderation
    private void handleAccept(Post post, VBox card) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Accept");
        confirm.setHeaderText(null);
        confirm.setContentText("Accept this post?\n\n\"" + post.getTitle() + "\"");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                post.setStatus("ACCEPTED");
                post.setRefusalReason(null);
                try {
                    postService.updatePostStatus(post);
                    // Remove card from view immediately
                    postsContainer.getChildren().remove(card);
                    refreshStatusLabel();
                    //Refresh all active Forum/FrontOffice views
                    FxApplication.refreshAllForumWindows();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Post Accepted",
                            "The post \"" + post.getTitle() + "\" has been accepted.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error",
                            "Could not update the post status:\n" + ex.getMessage());
                }
            }
        });
    }

    private void handleRefuse(Post post, VBox card) {
        // Ask for refusal reason via a TextArea dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Refuse Post");
        dialog.setHeaderText("Refusing: \"" + post.getTitle() + "\"");

        // Build dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(16));

        Label instruction = new Label("Please enter the reason for refusal:");
        instruction.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter refusal reason...");
        reasonArea.setWrapText(true);
        reasonArea.setPrefRowCount(4);
        reasonArea.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-radius: 8;"
        );

        content.getChildren().addAll(instruction, reasonArea);
        dialog.getDialogPane().setContent(content);

        ButtonType confirmBtn = new ButtonType("Refuse Post", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, cancelBtn);

        // Validate that a reason is entered before allowing confirm
        Node confirmNode = dialog.getDialogPane().lookupButton(confirmBtn);
        confirmNode.setDisable(true);
        reasonArea.textProperty().addListener((obs, oldVal, newVal) ->
                confirmNode.setDisable(newVal.trim().isEmpty())
        );

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmBtn) {
                return reasonArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(reason -> {
            post.setStatus("REFUSED");
            post.setRefusalReason(reason);
            try {
                postService.updatePostStatus(post);
                // Remove card from view immediately
                postsContainer.getChildren().remove(card);
                refreshStatusLabel();
                
                // --- INSTANT SYNC: Notify Forum windows ---
                FxApplication.refreshAllForumWindows();
                
                showAlert(Alert.AlertType.INFORMATION, "Post Refused",
                        "The post has been refused.\nReason: " + reason);
            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        "Could not update the post status:\n" + ex.getMessage());
            }
        });
    }


    @FXML
    private void showModeration() {
        setActiveNav(moderationNavButton);
        contentArea.getChildren().setAll(moderationView);
        loadPendingPosts();
    }

    @FXML
    private void showForum() {
        try {
            setActiveNav(forumNavButton);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/displayPost.fxml"));
            Parent forumRoot = loader.load();
            
            DisplayPostController controller = loader.getController();
            controller.setAdminMode(true);
            
            contentArea.getChildren().setAll(forumRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load forum view.");
        }
    }

    @FXML
    private void goToForum(javafx.event.ActionEvent event) {
        // Obsolete but kept for compatibility
        showForum();
    }


    private void refreshStatusLabel() {
        int remaining = postsContainer.getChildren().size();
        if (remaining == 0) {
            statusLabel.setText("No pending posts.");
            Label emptyLabel = new Label("✅  All posts have been moderated. No pending items.");
            emptyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b; -fx-padding: 24;");
            postsContainer.getChildren().add(emptyLabel);
        } else {
            statusLabel.setText(remaining + " post(s) pending review.");
        }
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "-";
        return date.format(DISPLAY_DATE_FORMAT);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
