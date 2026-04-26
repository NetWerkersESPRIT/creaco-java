package gui.forum;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class EmojiPickerModalController {

    @FXML private FlowPane smileysGrid;
    @FXML private FlowPane animalsGrid;
    @FXML private FlowPane foodGrid;

    private Consumer<String> onEmojiSelected;

    private final List<String> SMILEYS = Arrays.asList(
        "😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳"
    );

    private final List<String> ANIMALS = Arrays.asList(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆", "🦅", "🦉"
    );

    private final List<String> FOOD = Arrays.asList(
        "🍎", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌽", "🥕", "🫑", "🥔", "🍠", "🥐", "🍞"
    );

    @FXML
    public void initialize() {
        populateGrid(smileysGrid, SMILEYS);
        populateGrid(animalsGrid, ANIMALS);
        populateGrid(foodGrid, FOOD);
    }

    public void setOnEmojiSelected(Consumer<String> callback) {
        this.onEmojiSelected = callback;
    }

    private void populateGrid(FlowPane grid, List<String> emojis) {
        grid.getChildren().clear();
        for (String emoji : emojis) {
            Label label = new Label(emoji);
            label.setMinWidth(40);
            label.setMinHeight(40);
            label.setAlignment(javafx.geometry.Pos.CENTER);
            label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 5;");
            
            label.setOnMouseEntered(e -> label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-color: #f1f5f9; -fx-background-radius: 5;"));
            label.setOnMouseExited(e -> label.setStyle("-fx-font-size: 24px; -fx-text-fill: black; -fx-cursor: hand; -fx-background-radius: 5;"));
            
            label.setOnMouseClicked(e -> {
                if (onEmojiSelected != null) {
                    onEmojiSelected.accept(emoji);
                }
            });
            grid.getChildren().add(label);
        }
    }

    @FXML
    private void close() {
        ((Stage) smileysGrid.getScene().getWindow()).close();
    }
}
