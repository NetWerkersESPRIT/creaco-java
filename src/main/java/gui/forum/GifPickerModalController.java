package gui.forum;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.forum.GifService;

import java.util.List;
import java.util.function.Consumer;

public class GifPickerModalController {

    @FXML private TextField searchField;
    @FXML private FlowPane gifGrid;
    @FXML private VBox loadingOverlay;
    @FXML private VBox emptyState;

    private final GifService gifService = new GifService();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(400));
    private Consumer<String> onGifSelected;

    @FXML
    public void initialize() {
        setupSearch();
        loadTrending();
    }

    public void setOnGifSelected(Consumer<String> callback) {
        this.onGifSelected = callback;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            debounce.setOnFinished(e -> {
                if (newVal == null || newVal.trim().isEmpty()) {
                    loadTrending();
                } else {
                    performSearch(newVal.trim());
                }
            });
            debounce.playFromStart();
        });
    }

    @FXML
    public void loadTrending() {
        showLoading(true);
        gifService.getTrendingGifs().thenAccept(this::displayGifs);
    }

    private void performSearch(String query) {
        System.out.println("🔍 UI: Performing search for: " + query);
        showLoading(true);
        gifService.searchGifs(query).thenAccept(urls -> {
            System.out.println("🔍 UI: Received " + urls.size() + " GIFs for query: " + query);
            displayGifs(urls);
        });
    }

    private void displayGifs(List<String> urls) {
        Platform.runLater(() -> {
            gifGrid.getChildren().clear();
            showLoading(false);
            
            if (urls.isEmpty()) {
                emptyState.setVisible(true);
                return;
            }
            emptyState.setVisible(false);

            for (String url : urls) {
                gifGrid.getChildren().add(createGifView(url));
            }
        });
    }

    private ImageView createGifView(String url) {
        // WhatsApp style grid item
        double size = 110; 
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(false); // Force square for grid consistency like WhatsApp
        imageView.setSmooth(true);
        imageView.setStyle("-fx-cursor: hand; -fx-background-color: #f1f5f9; -fx-background-radius: 8;");

        // Use background loading to keep UI smooth
        Image img = new Image(url, size, size, false, true, true);
        imageView.setImage(img);

        imageView.setOnMouseClicked(e -> {
            if (onGifSelected != null) {
                onGifSelected.accept(url);
            }
            closeModal();
        });

        // Hover effect
        imageView.setOnMouseEntered(e -> imageView.setOpacity(0.8));
        imageView.setOnMouseExited(e -> imageView.setOpacity(1.0));

        return imageView;
    }

    private void showLoading(boolean loading) {
        loadingOverlay.setVisible(loading);
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) gifGrid.getScene().getWindow();
        stage.close();
    }
}
