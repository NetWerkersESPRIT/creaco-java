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
import main.FxApplication;

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

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();
    private final UserService userService = new UserService();
    
    private List<Post> allPosts = new ArrayList<>();
    
    private boolean isAdminMode = false;
    private boolean initialized = false;
    
    public void setAdminMode(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
        if (initialized) {
            loadPosts();
        }
    }

    @FXML
    public void initialize() {
        // Register this instance so Backoffice can refresh it
        FxApplication.registerForumController(this);
        initialized = true;
        loadPosts();
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
        
        //Pinned posts first, then newest ID first (descending)
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
            Label emptyState = new Label("No posts available matching your search.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
            postsContainer.getChildren().add(emptyState);
            return;
        }

        for (Post post : sortedPosts) {
            System.out.println("Rendering post: " + post.getTitle() + " | Image: " + post.getImageName());
            VBox card = buildPostCard(post);
            postsContainer.getChildren().add(card);
        }
    }

    private VBox buildPostCard(Post post) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 18;");

        // avatar
        entities.Users author = userService.getUserById(post.getUserId());
        String username = (author != null) ? author.getUsername() : "Unknown";
        HBox authorRow = new HBox(10);
        authorRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.StackPane avatar = buildAvatar(username);
        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #355388;");
        authorRow.getChildren().addAll(avatar, usernameLabel);

        // Add (admin)
        boolean isAdmin = (post.getUserId() == 5) || 
                          (author != null && author.getRole() != null && author.getRole().toLowerCase().contains("admin"));
        if (isAdmin) {
            Label adminLabel = new Label("(admin)");
            adminLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
            authorRow.getChildren().add(adminLabel);
        }

        HBox header = new HBox(16);
        VBox headerText = new VBox(8);
        
        Label titleLabel = new Label(safeText(post.getTitle()));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        // comment counting
        int commentCount = 0;
        try {
            commentCount = commentService.getCommentCountByPost(post.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String metaText = "Created " + formatDate(post.getCreatedAt()) + "  |  Comments: " + commentCount;
        Label metaLabel = new Label(metaText);
        metaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        headerText.getChildren().addAll(titleLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button commentsButton = createActionButton("View comments", "#355388", "#eef3fb");
        commentsButton.setOnAction(event -> openComments(event, post));

        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        editButton.setOnAction(event -> openEdit(event, post));

        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");
        deleteButton.setOnAction(event -> deletePost(post.getId()));

        actions.getChildren().add(commentsButton);

        int currentUserId = isAdminMode ? 5 : 1;
        boolean isOwner = (post.getUserId() == currentUserId);

        // Ownership-based Edit: Only author can edit
        if (isOwner) {
            actions.getChildren().add(editButton);
        }

        // Ownership or Admin-based Delete: Author or Admin can delete
        if (isOwner || isAdminMode) {
            actions.getChildren().add(deleteButton);
        }

        header.getChildren().addAll(headerText, spacer, actions);

        Label contentLabel = new Label(safeText(post.getContent()));
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        card.getChildren().addAll(authorRow, header, contentLabel);


        if (post.isPinned()) {
            Label pinnedBadge = new Label("📌 Pinned Post");
            pinnedBadge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; "
                    + "-fx-padding: 4 10; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 12px;");
            headerText.getChildren().add(1, pinnedBadge); // Add below title
        }

        // image
        if (post.getImageName() != null && !post.getImageName().isEmpty()) {
            try {
                String imagePath = "src/main/resources/uploads/images/" + post.getImageName();
                File file = new File(imagePath);
                
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(400);
                    imageView.setPreserveRatio(true);
                    
                    // Simplified clip
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                    clip.setArcWidth(20);
                    clip.setArcHeight(20);
                    
                    // Bind width
                    clip.widthProperty().bind(imageView.fitWidthProperty());
                    image.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() == 1.0) {
                            clip.setHeight(imageView.getBoundsInLocal().getHeight());
                        }
                    });
                    // Also set an initial height based on expected ratio or just large enough
                    clip.setHeight(300); 
                    
                    imageView.setClip(clip);
                    card.getChildren().add(imageView);
                    System.out.println("Image added to card: " + file.getAbsolutePath());
                } else {
                    System.out.println("Image file NOT found: " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                System.out.println("Exception while loading image: " + e.getMessage());
            }
        }

        //pdf
        if (post.getPdfName() != null && !post.getPdfName().isEmpty()) {
            Button openPdfButton = createActionButton("📄 Open Attachment (PDF)", "#1e293b", "#f1f5f9");
            openPdfButton.setOnAction(event -> {
                try {
                    File file = new File("src/main/resources/uploads/pdfs/" + post.getPdfName());
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert(Alert.AlertType.WARNING, "File Not Found", "The PDF file could not be found locally.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not open the PDF file.");
                }
            });
            card.getChildren().add(openPdfButton);
        }

        return card;
    }
    
    private javafx.scene.layout.StackPane buildAvatar(String username) {
        // Generate a consistent color from the first letter
        String[] colors = {"#355388", "#1e8a5e", "#7c3aed", "#c2410c", "#0369a1",
                           "#047857", "#b45309", "#9d174d", "#1d4ed8", "#065f46"};
        char initial = (username != null && !username.isEmpty()) ? Character.toUpperCase(username.charAt(0)) : '?';
        String color = colors[Math.abs(initial) % colors.length];

        Label initLabel = new Label(String.valueOf(initial));
        initLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        javafx.scene.layout.StackPane circle = new javafx.scene.layout.StackPane(initLabel);
        circle.setPrefSize(36, 36);
        circle.setMinSize(36, 36);
        circle.setMaxSize(36, 36);
        circle.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 50%;");
        return circle;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 10 16 10 16; -fx-font-weight: bold;");
        return button;
    }

    /**
     * Safely locates the #contentArea StackPane from any node in the scene.
     */
    private StackPane findContentArea(Node source) {
        StackPane area = (StackPane) source.getScene().lookup("#contentArea");
        if (area != null) return area;
        javafx.scene.Node parent = source.getParent();
        while (parent != null) {
            if (parent instanceof StackPane && "contentArea".equals(parent.getId())) {
                return (StackPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @FXML
    private void goAddPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/post/addPost.fxml"));
            Parent root = loader.load();
            
            AddPostController controller = loader.getController();
            controller.setAdminMode(this.isAdminMode);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deletePost(int id) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this post?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postService.supprimer(id);
                    // Refresh current view
                    loadPosts();
                    
                    // --- INSTANT SYNC: Notify Backoffice and other Forum windows ---
                    if (main.FxApplication.getBackofficeController() != null) {
                        main.FxApplication.getBackofficeController().loadPendingPosts();
                    }
                    main.FxApplication.refreshAllForumWindows();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete the post.");
                }
            }
        });
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
