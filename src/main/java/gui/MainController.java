package gui;

import entities.Comment;
import entities.Post;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import services.forum.CommentService;
import services.forum.PostService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainController {

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PostService postService = new PostService();
    private final CommentService commentService = new CommentService();

    private List<Post> posts = Collections.emptyList();
    private Map<Integer, List<Comment>> commentsByPost = Collections.emptyMap();
    private Integer expandedPostId;
    private Post editingPost;

    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label pageSubtitleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button forumNavButton;
    @FXML
    private Button coursesNavButton;
    @FXML
    private VBox forumSection;
    @FXML
    private VBox coursesSection;
    @FXML
    private TextField postTitleField;
    @FXML
    private TextArea postContentArea;
    @FXML
    private Button submitPostButton;
    @FXML
    private Button cancelEditButton;
    @FXML
    private VBox postsContainer;

    @FXML
    private void initialize() {
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);

        // Hide courses section
        coursesSection.setVisible(false);
        coursesSection.setManaged(false);
        coursesNavButton.setVisible(false);
        coursesNavButton.setManaged(false);

        // Show forum section
        forumSection.setVisible(true);
        forumSection.setManaged(true);

        // Apply active style to forum button
        forumNavButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #1f365c; "
                + "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;");

        pageTitleLabel.setText("Forum");
        pageSubtitleLabel.setText("Create posts, review discussions, and manage comments.");

        loadForumData();
    }

    @FXML
    private void showForum() {
        forumSection.setVisible(true);
        forumSection.setManaged(true);
        coursesSection.setVisible(false);
        coursesSection.setManaged(false);

        forumNavButton.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #1f365c; "
                + "-fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 14;");

        pageTitleLabel.setText("Forum");
        pageSubtitleLabel.setText("Create posts, review discussions, and manage comments.");

        loadForumData();
    }

    @FXML
    private void showCourses() {
        // Courses are disabled - just show forum instead
        showForum();
    }

    @FXML
    private void onSubmitPost() {
        String title = postTitleField.getText().trim();
        String content = postContentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title and content are required.");
            return;
        }
        if (title.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
            return;
        }

        try {
            if (editingPost == null) {
                Post post = new Post();
                post.setTitle(title);
                post.setContent(content);
                post.setStatus("Active");
                post.setUserId(1);
                postService.ajouter(post);
                statusLabel.setText("Post added successfully.");
            } else {
                editingPost.setTitle(title);
                editingPost.setContent(content);
                postService.modifier(editingPost.getId(), editingPost);
                statusLabel.setText("Post updated successfully.");
            }
            resetPostForm();
            loadForumData();
        } catch (SQLException exception) {
            statusLabel.setText("Database error: " + exception.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save the post.");
        }
    }

    @FXML
    private void onCancelEdit() {
        resetPostForm();
        statusLabel.setText("Edit cancelled.");
    }

    private void loadForumData() {
        try {
            posts = postService.afficher();
            commentsByPost = commentService.afficher().stream()
                    .collect(Collectors.groupingBy(Comment::getPostId));
            int commentCount = commentsByPost.values().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(List::size)
                    .sum();
            statusLabel.setText(posts.size() + " post(s), " + commentCount + " comment(s).");
        } catch (SQLException exception) {
            posts = Collections.emptyList();
            commentsByPost = Collections.emptyMap();
            statusLabel.setText("Database error: " + exception.getMessage());
        }
        renderPosts();
    }

    private void renderPosts() {
        postsContainer.getChildren().clear();

        if (posts.isEmpty()) {
            Label emptyState = new Label("No posts available yet. Create the first discussion.");
            emptyState.setStyle("-fx-font-size: 15px; -fx-text-fill: #64748b;");
            postsContainer.getChildren().add(emptyState);
            return;
        }

        for (Post post : posts) {
            postsContainer.getChildren().add(buildPostCard(post));
        }
    }

    private Node buildPostCard(Post post) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 18;");

        HBox header = new HBox(16);
        VBox headerText = new VBox(8);
        Label titleLabel = new Label(safeText(post.getTitle()));
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #243b63;");

        Label metaLabel = new Label(buildPostMeta(post));
        metaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        headerText.getChildren().addAll(titleLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        Button commentsButton = createActionButton(
                isExpanded(post) ? "Hide comments" : "View comments",
                "#355388",
                "#eef3fb"
        );
        commentsButton.setOnAction(event -> toggleComments(post));

        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        editButton.setOnAction(event -> startEditingPost(post));

        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");
        deleteButton.setOnAction(event -> deletePost(post));

        actions.getChildren().addAll(commentsButton, editButton, deleteButton);
        header.getChildren().addAll(headerText, spacer, actions);

        Label contentLabel = new Label(safeText(post.getContent()));
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        HBox statsRow = new HBox(18);
        statsRow.getChildren().addAll(
                buildInfoChip("Status", safeText(post.getStatus())),
                buildInfoChip("Comments", String.valueOf(getComments(post).size())),
                buildInfoChip("Created", formatDate(post.getCreatedAt()))
        );

        card.getChildren().addAll(header, contentLabel, statsRow);

        if (isExpanded(post)) {
            card.getChildren().add(buildCommentSection(post));
        }

        return card;
    }

    private Node buildCommentSection(Post post) {
        VBox section = new VBox(18);
        section.setPadding(new Insets(22, 0, 0, 0));

        VBox shell = new VBox(18);
        shell.setPadding(new Insets(20));
        shell.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 18; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 18;");

        HBox header = new HBox();
        Label sectionTitle = new Label("Comments");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #243b63;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label counter = new Label(getComments(post).size() + " item(s)");
        counter.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
        header.getChildren().addAll(sectionTitle, spacer, counter);

        VBox commentComposer = new VBox(8);
        Label composerLabel = new Label("Add comment");
        composerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write a comment for this post...");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);

        HBox composerActions = new HBox(12);
        Region composerSpacer = new Region();
        HBox.setHgrow(composerSpacer, Priority.ALWAYS);
        Button addCommentButton = createPrimaryButton("Publish comment");
        addCommentButton.setOnAction(event -> addComment(post, commentArea));
        composerActions.getChildren().addAll(composerSpacer, addCommentButton);
        commentComposer.getChildren().addAll(composerLabel, commentArea, composerActions);

        VBox commentsBox = new VBox(12);
        List<Comment> postComments = getComments(post);
        if (postComments.isEmpty()) {
            Label emptyState = new Label("No comments yet.");
            emptyState.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            commentsBox.getChildren().add(emptyState);
        } else {
            for (Comment comment : postComments) {
                commentsBox.getChildren().add(buildCommentCard(post, comment));
            }
        }

        shell.getChildren().addAll(header, commentComposer, commentsBox);
        section.getChildren().add(shell);
        return section;
    }

    private Node buildCommentCard(Post post, Comment comment) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 14;");

        Label bodyLabel = new Label(safeText(comment.getBody()));
        bodyLabel.setWrapText(true);
        bodyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        HBox footer = new HBox(10);
        Label meta = new Label("Posted " + formatDate(comment.getCreatedAt()));
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = createActionButton("Edit", "#355388", "#eef3fb");
        editButton.setOnAction(event -> editComment(comment));
        Button deleteButton = createActionButton("Delete", "#c62828", "#fdecec");
        deleteButton.setOnAction(event -> deleteComment(post, comment));

        footer.getChildren().addAll(meta, spacer, editButton, deleteButton);
        card.getChildren().addAll(bodyLabel, footer);
        return card;
    }

    private void toggleComments(Post post) {
        expandedPostId = isExpanded(post) ? null : post.getId();
        renderPosts();
        statusLabel.setText("Viewing discussion for \"" + safeText(post.getTitle()) + "\".");
    }

    private void startEditingPost(Post post) {
        editingPost = post;
        postTitleField.setText(safeText(post.getTitle()));
        postContentArea.setText(safeText(post.getContent()));
        submitPostButton.setText("Save changes");
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
        statusLabel.setText("Editing post \"" + safeText(post.getTitle()) + "\".");
    }

    private void resetPostForm() {
        editingPost = null;
        postTitleField.clear();
        postContentArea.clear();
        submitPostButton.setText("Add post");
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
    }

    private void addComment(Post post, TextArea commentArea) {
        String body = commentArea.getText().trim();
        if (body.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment cannot be empty.");
            return;
        }
        if (body.length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Comment must be at least 2 characters.");
            return;
        }

        Comment comment = new Comment();
        comment.setBody(body);
        comment.setStatus("Active");
        comment.setPostId(post.getId());
        comment.setUserId(1);

        try {
            commentService.ajouter(comment);
            expandedPostId = post.getId();
            loadForumData();
            statusLabel.setText("Comment added successfully.");
            commentArea.clear();
        } catch (SQLException exception) {
            statusLabel.setText("Database error: " + exception.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save the comment.");
        }
    }

    private void editComment(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(safeText(comment.getBody()));
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Update the comment text.");
        dialog.setContentText("Content:");
        dialog.showAndWait()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .ifPresent(updatedBody -> {
                    comment.setBody(updatedBody);
                    try {
                        commentService.modifier(comment.getId(), comment);
                        loadForumData();
                        statusLabel.setText("Comment updated successfully.");
                    } catch (SQLException exception) {
                        statusLabel.setText("Database error: " + exception.getMessage());
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update the comment.");
                    }
                });
    }

    private void deletePost(Post post) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this post?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    postService.supprimer(post.getId());
                    if (Objects.equals(expandedPostId, post.getId())) {
                        expandedPostId = null;
                    }
                    if (editingPost != null && editingPost.getId() == post.getId()) {
                        resetPostForm();
                    }
                    loadForumData();
                    statusLabel.setText("Post deleted successfully.");
                } catch (SQLException exception) {
                    statusLabel.setText("Database error: " + exception.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete the post.");
                }
            }
        });
    }

    private void deleteComment(Post post, Comment comment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this comment?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentService.supprimer(comment.getId());
                    expandedPostId = post.getId();
                    loadForumData();
                    statusLabel.setText("Comment deleted successfully.");
                } catch (SQLException exception) {
                    statusLabel.setText("Database error: " + exception.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Could not delete the comment.");
                }
            }
        });
    }

    private List<Comment> getComments(Post post) {
        return commentsByPost.getOrDefault(post.getId(), Collections.emptyList());
    }

    private boolean isExpanded(Post post) {
        return expandedPostId != null && expandedPostId == post.getId();
    }

    private String buildPostMeta(Post post) {
        return "Status: " + safeText(post.getStatus())
                + "  |  User #" + post.getUserId()
                + "  |  Created " + formatDate(post.getCreatedAt());
    }

    private VBox buildInfoChip(String labelText, String valueText) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 12; "
                + "-fx-border-color: #dbe4f0; -fx-border-radius: 12;");

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        Label value = new Label(valueText);
        value.setWrapText(true);
        value.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-font-weight: bold;");
        box.getChildren().addAll(label, value);
        return box;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #355388; -fx-text-fill: white; "
                + "-fx-background-radius: 12; -fx-padding: 10 18 10 18; -fx-font-weight: bold;");
        return button;
    }

    private Button createActionButton(String text, String textColor, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: " + textColor
                + "; -fx-background-radius: 12; -fx-padding: 10 16 10 16; -fx-font-weight: bold;");
        return button;
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "-";
        }
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