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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.forum.CommentService;
import services.forum.PostService;

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
    
    private List<Post> allPosts = new ArrayList<>();

    @FXML
    public void initialize() {
        loadPosts();
    }

    private void loadPosts() {
        try {
            allPosts = postService.afficher();
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
        statusLabel.setText(postsToDisplay.size() + " post(s) found.");

        if (postsToDisplay.isEmpty()) {
            Label emptyState = new Label("No posts available matching your search.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
            postsContainer.getChildren().add(emptyState);
            return;
        }

        for (Post post : postsToDisplay) {
            VBox card = buildPostCard(post);
            postsContainer.getChildren().add(card);
        }
    }

    private VBox buildPostCard(Post post) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 18;");

        HBox header = new HBox(16);
        VBox headerText = new VBox(8);
        
        Label titleLabel = new Label(safeText(post.getTitle()));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        // Display title, content, created_at and COMMENT COUNT
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

        actions.getChildren().addAll(commentsButton, editButton, deleteButton);
        header.getChildren().addAll(headerText, spacer, actions);

        Label contentLabel = new Label(safeText(post.getContent()));
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        card.getChildren().addAll(header, contentLabel);

        return card;
    }
    
    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 10 16 10 16; -fx-font-weight: bold;");
        return button;
    }

    @FXML
    private void goAddPost(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/post/addPost.fxml"));
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
            controller.setPost(post);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openComments(ActionEvent event, Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/comment/displayComment.fxml"));
            Parent root = loader.load();
            
            DisplayCommentController controller = loader.getController();
            controller.setPost(post);
            
            StackPane contentArea = (StackPane) ((Node) event.getSource()).getScene().lookup("#contentArea");
            contentArea.getChildren().setAll(root);
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
                    loadPosts();
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
