package gui.chat;

import entities.Users;
import entities.forum.Conversation;
import entities.forum.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import services.UsersService;
import services.NotificationService;
import services.forum.MessageService;
import utils.SessionManager;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloatingChatController {

    @FXML private VBox chatRoot;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private ScrollPane scrollPane;
    @FXML private Label userNameLabel;
    @FXML private ImageView headerAvatar;

    private final MessageService messageService = new MessageService();
    private final UsersService usersService = new UsersService();
    private final NotificationService notificationService = new NotificationService();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private int conversationId;
    private Users currentUser;
    private Conversation conversation;
    private Runnable onCloseCallback;
    private int otherUserId;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));
        
        Circle clip = new Circle(18, 18, 18);
        headerAvatar.setClip(clip);
        
        messagesContainer.setFocusTraversable(false);
        scrollPane.setFocusTraversable(false);
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
        loadConversation();
    }

    public void setOnClose(Runnable callback) {
        this.onCloseCallback = callback;
    }

    private void loadConversation() {
        try {
            this.conversation = messageService.getConversationById(conversationId);
            if (conversation != null && currentUser != null) {
                otherUserId = (conversation.getOwnerUserId() == currentUser.getId()) ? conversation.getAdminUserId() : conversation.getOwnerUserId();
                Users otherUser = usersService.getUserById(otherUserId);
                
                if (otherUser != null) {
                    String name = otherUser.getUsername() != null ? otherUser.getUsername() : "User";
                    String role = otherUser.getRole() != null ? otherUser.getRole().replace("ROLE_", "") : "USER";
                    role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                    userNameLabel.setText(name + " (" + role + ")");
                    
                    String avatarUrl = otherUser.getImage();
                    if (avatarUrl == null || avatarUrl.isEmpty()) {
                        avatarUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + name;
                    }
                    headerAvatar.setImage(new Image(avatarUrl, true));
                }
                loadMessages();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        try {
            List<Message> messages = messageService.getMessagesByConversation(conversationId);
            for (Message m : messages) {
                messagesContainer.getChildren().add(createMessageBubble(m));
            }
            messageService.markMessagesAsRead(conversationId, currentUser.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createMessageBubble(Message message) {
        boolean isMe = (currentUser != null && message.getSenderUserId() == currentUser.getId());
        Users sender = null;
        try {
            sender = usersService.getUserById(message.getSenderUserId());
        } catch (SQLException e) {}

        HBox hBox = new HBox(8);
        hBox.setAlignment(isMe ? Pos.TOP_RIGHT : Pos.TOP_LEFT);
        hBox.setPadding(new Insets(5, 0, 5, 0));

        ImageView avatar = new ImageView();
        avatar.setFitHeight(28);
        avatar.setFitWidth(28);
        String avatarUrl = (sender != null && sender.getImage() != null) ? sender.getImage() : "https://api.dicebear.com/7.x/avataaars/png?seed=" + (sender != null ? sender.getUsername() : "User");
        avatar.setImage(new Image(avatarUrl, true));
        Circle clip = new Circle(14, 14, 14);
        avatar.setClip(clip);

        VBox contentBox = new VBox(2);
        contentBox.setAlignment(isMe ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        Label nameLabel = new Label(sender != null ? sender.getUsername() : "User");
        nameLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold;");
        
        StackPane bubbleStack = new StackPane();
        bubbleStack.setAlignment(isMe ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
        bubbleStack.setFocusTraversable(false);

        String content = message.getContent() != null ? message.getContent() : "";
        
        if (content.contains("[IMAGE]")) {
            String path = content.substring(content.indexOf("[IMAGE]") + 7);
            File file = new File(path);
            if (file.exists()) {
                ImageView imgView = new ImageView(new Image(file.toURI().toString(), true));
                imgView.setFitWidth(180);
                imgView.setPreserveRatio(true);
                VBox imgContainer = new VBox(imgView);
                imgContainer.setMouseTransparent(true);
                bubbleStack.getChildren().add(imgContainer);
                bubbleStack.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 15; -fx-padding: 5;");
            } else {
                Label err = new Label("Image Not Found");
                err.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 15; -fx-padding: 10; -fx-font-size: 11px;");
                bubbleStack.getChildren().add(err);
            }
        } else if (content.contains("[FILE]")) {
            String path = content.substring(content.indexOf("[FILE]") + 6);
            File file = new File(path);
            Label fileLabel = new Label("📄 " + file.getName());
            fileLabel.setMouseTransparent(true);
            bubbleStack.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 15; -fx-padding: 8 12;");
            fileLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12px;");
            bubbleStack.getChildren().add(fileLabel);
            bubbleStack.setCursor(javafx.scene.Cursor.HAND);
            bubbleStack.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    try { java.awt.Desktop.getDesktop().open(file); } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
        } else if (content.contains("[AUDIO]")) {
            Label audioLabel = new Label("🔊 Voice Note");
            audioLabel.setMouseTransparent(true);
            bubbleStack.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 15; -fx-padding: 8 12;");
            audioLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12px;");
            bubbleStack.getChildren().add(audioLabel);
        } else {
            Label bubble = new Label(content);
            bubble.setWrapText(true);
            bubble.setMaxWidth(200);
            bubble.setMouseTransparent(true);
            
            if (isMe) {
                bubbleStack.setStyle("-fx-background-color: linear-gradient(to bottom right, #ce2d7c, #6c2db1); -fx-background-radius: 18 18 2 18; -fx-padding: 10 14;");
                bubble.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
            } else {
                bubbleStack.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 18 18 18 2; -fx-padding: 10 14;");
                bubble.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px;");
            }
            bubbleStack.getChildren().add(bubble);
        }

        String timeStr = (message.getCreatedAt() != null) ? message.getCreatedAt().format(timeFormatter) : "";
        String status = message.isRead() ? "Seen" : "Delivered";
        Label infoLabel = new Label(timeStr + " • " + status);
        infoLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9px;");

        contentBox.getChildren().addAll(nameLabel, bubbleStack, infoLabel);

        if (isMe) {
            hBox.getChildren().addAll(contentBox, avatar);
        } else {
            hBox.getChildren().addAll(avatar, contentBox);
        }

        return hBox;
    }

    @FXML
    private void handleSend() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentUser == null) return;
        try {
            messageService.addMessage(conversationId, currentUser.getId(), content);
            messageInput.clear();
            loadMessages();
            notificationService.notifyNewMessage(otherUserId, currentUser.getUsername(), conversationId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach File");
        File selectedFile = fileChooser.showOpenDialog(chatRoot.getScene().getWindow());
        
        if (selectedFile != null) {
            String prefix = "[FILE]";
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                prefix = "[IMAGE]";
            }
            try {
                messageService.addMessage(conversationId, currentUser.getId(), prefix + selectedFile.getAbsolutePath());
                loadMessages();
                notificationService.notifyNewMessage(otherUserId, currentUser.getUsername(), conversationId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClose() {
        if (onCloseCallback != null) onCloseCallback.run();
    }

    @FXML
    private void onHeaderPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onHeaderDragged(MouseEvent event) {
        chatRoot.setTranslateX(chatRoot.getTranslateX() + (event.getSceneX() - xOffset));
        chatRoot.setTranslateY(chatRoot.getTranslateY() + (event.getSceneY() - yOffset));
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
}
