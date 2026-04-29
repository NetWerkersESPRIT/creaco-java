package gui.forum;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import services.forum.EmojiService;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmojiPickerModalController {

    @FXML private TabPane categoryTabPane;
    @FXML private FlowPane smileysGrid;
    @FXML private FlowPane animalsGrid;
    @FXML private FlowPane foodGrid;

    private final EmojiService emojiService = new EmojiService();
    private Consumer<String> onEmojiSelected;
    private List<EmojiService.Emoji> allEmojis;

    @FXML
    public void initialize() {
        loadInitialEmojis();
    }

    public void setOnEmojiSelected(Consumer<String> callback) {
        this.onEmojiSelected = callback;
    }

    private void loadInitialEmojis() {
        emojiService.fetchAllEmojis().thenAccept(emojis -> {
            this.allEmojis = emojis;
            Platform.runLater(() -> {
                populateCategory(smileysGrid, "smileys and people");
                populateCategory(animalsGrid, "animals and nature");
                populateCategory(foodGrid, "food and drink");
            });
        });
    }

    private void populateCategory(FlowPane grid, String category) {
        if (allEmojis == null) return;
        List<EmojiService.Emoji> filtered = allEmojis.stream()
                .filter(e -> e.category.equalsIgnoreCase(category))
                .limit(80) // Optimized limit
                .collect(Collectors.toList());
        displayEmojiList(grid, filtered);
    }

    private void displayEmojiList(FlowPane grid, List<EmojiService.Emoji> emojis) {
        grid.getChildren().clear();
        for (EmojiService.Emoji emoji : emojis) {
            Label label = new Label(emoji.character);
            label.setMinWidth(42);
            label.setMinHeight(42);
            label.setAlignment(javafx.geometry.Pos.CENTER);
            label.setTooltip(new Tooltip(emoji.name));
            label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 8;");
            
            label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-color: #f1f5f9; -fx-background-radius: 8;"));
            label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 8;"));
            
            label.setOnMouseClicked(e -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(emoji.character);
                }
            });
            grid.getChildren().add(label);
        }
    }

    @FXML
    private void close() {
        ((Stage) categoryTabPane.getScene().getWindow()).close();
    }
}
