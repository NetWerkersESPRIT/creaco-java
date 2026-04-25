package gui.post;

import entities.Post;
import gui.comment.DisplayCommentController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.forum.CommentService;
import services.forum.PostService;
import services.UserService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayPostController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private VBox postsContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final UserService userService = new UserService();
    
    private List<Post> allPosts = new ArrayList<>();
    
    private boolean isAdminMode = false;
    private boolean initialized = false;
    private final java.util.Set<Integer> likedPosts = new java.util.HashSet<>();
    
    public void setAdminMode(boolean isAdminMode) {
        // The session now strictly controls admin mode
        // this.isAdminMode = isAdminMode;
        if (initialized) {
            loadPosts();
        }
    }

    @FXML
    public void initialize() {
        this.isAdminMode = utils.SessionManager.getInstance().isAdmin();
        initialized = true;
        loadPosts();
        
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

    public void loadPosts() {
        try {
            allPosts = postService.getAcceptedPosts();
            renderPosts(allPosts);
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Database error: Could not load posts.");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not load posts.");
        }
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> post.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toList());
        renderPosts(filteredPosts);
    }

    private void renderPosts(List<Post> postsToDisplay) {
        postsContainer.getChildren().clear();
        
        List<Post> sortedPosts = postsToDisplay.stream()
                .sorted((p1, p2) -> {
                    if (p1.isPinned() != p2.isPinned()) {
                        return Boolean.compare(p2.isPinned(), p1.isPinned());
                    }
                    return Integer.compare(p2.getId(), p1.getId());
                })
                .collect(Collectors.toList());

        statusLabel.setText(sortedPosts.size() + " post(s) found.");

        if (sortedPosts.isEmpty()) {
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPadding(new Insets(100, 0, 100, 0));
            emptyState.getStyleClass().add("card");
            
            StackPane iconCircle = new StackPane();
            iconCircle.setPrefSize(60, 60);
            iconCircle.setMaxSize(60, 60);
            iconCircle.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 50;");
            Label icon = new Label("🔍");
            icon.setStyle("-fx-font-size: 24px;");
            iconCircle.getChildren().add(icon);
            
            Label mainMsg = new Label("Nothing here yet");
            mainMsg.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            
            Label subMsg = new Label("Start the discussion and be the first to post!");
            subMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #718096;");
            
            emptyState.getChildren().addAll(iconCircle, mainMsg, subMsg);
            postsContainer.getChildren().add(emptyState);
            return;
        }

        for (Post post : sortedPosts) {
            VBox card = buildPostCard(post);
            postsContainer.getChildren().add(card);
        }
    }

    private VBox buildPostCard(Post post) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(25));
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        // --- Author Row ---
        entities.Users author = userService.getUserById(post.getUserId());
        String username = (author != null) ? author.getUsername() : "Unknown";
        HBox authorRow = new HBox(12);
        authorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        StackPane avatar = buildAvatar(username);
        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        authorRow.getChildren().addAll(avatar, usernameLabel);

        if (post.isPinned()) {
            Label pinnedBadge = new Label("📌 PINNED");
            pinnedBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #ce2d7c; -fx-font-weight: bold;");
            authorRow.getChildren().add(pinnedBadge);
        }

        // --- Title & Content ---
        VBox body = new VBox(8);
        Label titleLabel = new Label(safeText(post.getTitle()));
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Label contentLabel = new Label(safeText(post.getContent()));
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #718096; -fx-line-spacing: 4;");
        body.getChildren().addAll(titleLabel, contentLabel);

        card.getChildren().addAll(authorRow, body);

        // Handle Image
        if (post.getImageName() != null && !post.getImageName().isEmpty()) {
            ImageView imageView = buildImageView(post.getImageName());
            if (imageView != null) {
                card.getChildren().add(card.getChildren().size(), imageView);
            }
        }

        // --- Actions Row ---
        HBox actionsRow = new HBox(25);
        actionsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        actionsRow.getChildren().addAll(
            createLikeAction(post),
            createCommentAction(post),
            createReadAction(post)
        );

        entities.Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
        int currentUserId = (currentUser != null) ? currentUser.getId() : -1;
        boolean isOwner = (post.getUserId() == currentUserId);

        if (isOwner) {
            HBox editBtn = createSimpleAction("📝 Edit", "");
            editBtn.setOnMouseClicked(e -> openEdit(new ActionEvent(editBtn, null), post));
            editBtn.setStyle("-fx-cursor: hand;");
            actionsRow.getChildren().add(editBtn);
        }

        if (isOwner || isAdminMode) {
            HBox deleteBtn = createSimpleAction("🗑 Delete", "");
            deleteBtn.setOnMouseClicked(e -> deletePost(post.getId()));
            deleteBtn.setStyle("-fx-cursor: hand;");
            actionsRow.getChildren().add(deleteBtn);
        }

        if (isAdminMode) {
            actionsRow.getChildren().add(createPinAction(post));
        }
        actionsRow.getChildren().add(createSimpleAction("↪ Share", ""));

        card.getChildren().add(actionsRow);

        // PDF Attachment
        if (post.getPdfName() != null && !post.getPdfName().isEmpty()) {
            Button pdfBtn = createActionButton("📄 Open PDF", "#1e293b", "#f1f5f9");
            pdfBtn.setOnAction(e -> openPdf(post.getPdfName()));
            card.getChildren().add(pdfBtn);
        }

        return card;
    }

    private HBox createSimpleAction(String icon, String text) {
        HBox box = new HBox(6);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconL = new Label(icon);
        iconL.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        Label textL = new Label(text);
        textL.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        box.getChildren().addAll(iconL, textL);
        return box;
    }

    private HBox createLikeAction(Post post) {
        HBox box = createSimpleAction("👍 Like", String.valueOf(post.getLikes()));
        Label iconLabel = (Label) box.getChildren().get(0);
        Label countLabel = (Label) box.getChildren().get(1);

        if (likedPosts.contains(post.getId())) {
            iconLabel.setStyle("-fx-text-fill: #ce2d7c; -fx-font-weight: bold;");
            countLabel.setStyle("-fx-text-fill: #ce2d7c; -fx-font-weight: bold;");
        }

        box.setOnMouseClicked(e -> {
            try {
                if (likedPosts.contains(post.getId())) {
                    likedPosts.remove(post.getId());
                    post.setLikes(post.getLikes() - 1);
                    iconLabel.setStyle("-fx-text-fill: #64748b;");
                    countLabel.setStyle("-fx-text-fill: #64748b;");
                } else {
                    likedPosts.add(post.getId());
                    post.setLikes(post.getLikes() + 1);
                    iconLabel.setStyle("-fx-text-fill: #ce2d7c; -fx-font-weight: bold;");
                    countLabel.setStyle("-fx-text-fill: #ce2d7c; -fx-font-weight: bold;");
                }
                postService.likePost(post.getId(), post.getLikes());
                countLabel.setText(String.valueOf(post.getLikes()));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        box.setStyle("-fx-cursor: hand;");
        return box;
    }

    private HBox createCommentAction(Post post) {
        int count = 0;
        try { count = commentService.getCommentCountByPost(post.getId()); } catch (SQLException e) {}
        HBox box = createSimpleAction("💬", count + " Comments");
        box.setOnMouseClicked(e -> openComments(new ActionEvent(box, null), post));
        box.setStyle("-fx-cursor: hand;");
        return box;
    }

    private HBox createReadAction(Post post) {
        HBox box = createSimpleAction("📢 Read", "");
        box.setOnMouseClicked(e -> {
            String textToSpeak = "Post Title: " + post.getTitle() + ". Content: " + post.getContent();
            speak(textToSpeak);
        });
        box.setStyle("-fx-cursor: hand;");
        return box;
    }

    private void speak(String text) {
        new Thread(() -> {
            try {
                // Escape single quotes for PowerShell
                String escapedText = text.replace("'", "''").replace("\"", "");
                String script = "Add-Type -AssemblyName System.Speech; "
                        + "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                        + "$speak.Speak('" + escapedText + "')";
                new ProcessBuilder("powershell.exe", "-Command", script).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createPinAction(Post post) {
        HBox box = createSimpleAction("📌 Pin", "");
        box.setOnMouseClicked(e -> togglePin(post));
        box.setStyle("-fx-cursor: hand;");
        return box;
    }

    private void togglePin(Post post) {
        try {
            int userId = utils.SessionManager.getInstance().getCurrentUser().getId();
            String alertMessage = postService.handleTogglePin(post, isAdminMode, userId);
            
            if (alertMessage != null) {
                showAlert(Alert.AlertType.INFORMATION, "Pin Action", alertMessage);
            }
            
            loadPosts(); // Refresh list to show pinned at top
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Pin Denied", e.getMessage());
        }
    }

    private ImageView buildImageView(String imageName) {
        try {
            File file = new File("src/main/resources/uploads/images/" + imageName);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(500);
                imageView.setPreserveRatio(true);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                clip.setArcWidth(20); clip.setArcHeight(20);
                clip.widthProperty().bind(imageView.fitWidthProperty());
                image.progressProperty().addListener((obs, old, val) -> {
                    if (val.doubleValue() == 1.0) clip.setHeight(imageView.getBoundsInLocal().getHeight());
                });
                imageView.setClip(clip);
                return imageView;
            }
        } catch (Exception e) {}
        return null;
    }

    private StackPane buildAvatar(String username) {
        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ce2d7c;");
        StackPane circle = new StackPane(initLabel);
        circle.setPrefSize(35, 35); circle.setMinSize(35, 35); circle.setMaxSize(35, 35);
        circle.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 50;");
        return circle;
    }

    private void openPdf(String pdfName) {
        try {
            File file = new File("src/main/resources/uploads/pdfs/" + pdfName);
            if (file.exists()) Desktop.getDesktop().open(file);
            else showAlert(Alert.AlertType.WARNING, "Not Found", "PDF not found.");
        } catch (IOException e) { showAlert(Alert.AlertType.ERROR, "Error", "Could not open PDF."); }
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 10; -fx-padding: 8 16; -fx-font-weight: bold; -fx-font-size: 11px;");
        button.setCursor(javafx.scene.Cursor.HAND);
        return button;
    }

    private StackPane findContentArea(Node source) {
        StackPane area = (StackPane) source.getScene().lookup("#contentArea");
        if (area != null) return area;
        Node parent = source.getParent();
        while (parent != null) {
            if (parent instanceof StackPane && "contentArea".equals(parent.getId())) return (StackPane) parent;
            parent = parent.getParent();
        }
        return null;
    }

    @FXML
    public void goAddPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/addPost.fxml"));
            Parent root = loader.load();
            AddPostController controller = loader.getController();
            if (controller != null) controller.setAdminMode(this.isAdminMode);
            StackPane contentArea = findContentArea((Node) event.getSource());
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (Exception e) { e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Error", "Could not load Add Post."); }
    }

    private void openEdit(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/updatePost.fxml"));
            Parent root = loader.load();
            UpdatePostController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            controller.setPost(post);
            StackPane contentArea = findContentArea((Node) event.getSource());
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openComments(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();
            DisplayCommentController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            controller.setPost(post);
            StackPane contentArea = findContentArea((Node) event.getSource());
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void deletePost(int id) {
        if (gui.util.AlertHelper.showCustomAlert("Delete?", "Are you sure?", gui.util.AlertHelper.AlertType.CONFIRMATION)) {
            try { postService.supprimer(id); loadPosts(); } 
            catch (SQLException e) { showAlert(Alert.AlertType.ERROR, "Error", "Could not delete."); }
        }
    }

    private String formatDate(LocalDateTime date) { return (date == null) ? "-" : date.format(DISPLAY_DATE_FORMAT); }
    private String safeText(String value) { return (value == null || value.isBlank()) ? "-" : value; }
    private void showAlert(Alert.AlertType type, String title, String content) {
        gui.util.AlertHelper.AlertType ct = (type == Alert.AlertType.ERROR) ? gui.util.AlertHelper.AlertType.ERROR : gui.util.AlertHelper.AlertType.INFORMATION;
        gui.util.AlertHelper.showCustomAlert(title, content, ct);
    }
    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try {
            StackPane area = findContentArea((Node) event.getSource());
            if (area != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/Profile.fxml"));
                area.getChildren().setAll((Node) loader.load());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML public void logout(ActionEvent event) { gui.SessionHelper.logout(event); }
}
