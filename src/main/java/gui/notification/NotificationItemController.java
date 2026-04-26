package gui.notification;

import entities.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.time.Duration;
import java.time.LocalDateTime;

public class NotificationItemController {

    @FXML private HBox itemRoot;
    @FXML private StackPane iconContainer;
    @FXML private Label iconLabel;
    @FXML private Label messageLabel;
    @FXML private Label timeLabel;
    @FXML private Circle unreadDot;

    public void setData(Notification n) {
        messageLabel.setText(n.getMessage());
        timeLabel.setText(formatRelativeTime(n.getCreatedAt()));
        unreadDot.setVisible(!n.isRead());
        
        // Background color based on read status
        if (!n.isRead()) {
            itemRoot.setStyle(itemRoot.getStyle() + "; -fx-background-color: #fdf2f8;"); // Very light pink for unread
        }

        // Custom icons based on type
        switch (n.getType()) {
            case "WELCOME":
                iconLabel.setText("🎉");
                iconContainer.setPrefWidth(24);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #a855f7, #ec4899);");
                break;
            case "POST_APPROVED":
                iconLabel.setText("⋯");
                iconLabel.setRotate(90);
                iconContainer.setPrefWidth(24);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #ec4899, #be185d);");
                break;
            case "POST_PENDING":
                iconLabel.setText("🔔");
                iconContainer.setPrefWidth(48);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #a855f7, #ec4899);");
                break;
            case "COMMENT":
                iconLabel.setText("💬");
                iconContainer.setPrefWidth(48);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb);");
                break;
            case "REPLY":
                iconLabel.setText("↩️");
                iconContainer.setPrefWidth(48);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #f59e0b, #d97706);");
                break;
            case "LIKE_POST":
            case "LIKE_COMMENT":
                iconLabel.setText("❤️");
                iconContainer.setPrefWidth(48);
                iconContainer.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to bottom, #fb7185, #e11d48);");
                break;
            default:
                iconLabel.setText("🔔");
                iconContainer.setPrefWidth(48);
        }
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m";
        if (seconds < 86400) return (seconds / 3600) + "h";
        return (seconds / 86400) + "d";
    }
}
