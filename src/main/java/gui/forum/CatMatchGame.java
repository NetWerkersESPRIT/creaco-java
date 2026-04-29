package gui.forum;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import utils.QuoteService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatMatchGame extends StackPane {
    private static final String[] EMOJIS = {"😸", "🙀", "😻", "😹"};
    private static final int ROWS = 2;
    private static final int COLS = 4;

    private Card firstCard = null;
    private Card secondCard = null;
    private int pairsFound = 0;
    private boolean isAnimating = false;
    private Runnable onComplete;
    private final QuoteService quoteService = new QuoteService();

    public CatMatchGame(Runnable onComplete) {
        this.onComplete = onComplete;
        setupUI();
    }

    private void setupUI() {
        this.setStyle("-fx-background-color: #0f172a;");
        this.setPadding(new Insets(40));

        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        Label icon = new Label("🐱");
        icon.setStyle("-fx-font-size: 30px; -fx-background-color: #f59e0b; -fx-background-radius: 50; -fx-padding: 5;");
        Label title = new Label("Cat-tastic Match");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitle = new Label("FIND ALL 4 MATCHING PAIRS");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");
        header.getChildren().addAll(icon, title, subtitle);

        // Grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        List<String> deck = new ArrayList<>();
        for (String e : EMOJIS) {
            deck.add(e);
            deck.add(e);
        }
        Collections.shuffle(deck);

        for (int i = 0; i < deck.size(); i++) {
            Card card = new Card(deck.get(i));
            grid.add(card, i % COLS, i / COLS);
        }

        layout.getChildren().addAll(header, grid);
        this.getChildren().add(layout);
    }

    private class Card extends StackPane {
        private String emoji;
        private Label label;
        private Rectangle back;
        private boolean isFlipped = false;
        private boolean isMatched = false;

        public Card(String emoji) {
            this.emoji = emoji;
            this.setPrefSize(100, 120);
            this.setCursor(javafx.scene.Cursor.HAND);

            back = new Rectangle(100, 120);
            back.setArcWidth(20);
            back.setArcHeight(20);
            back.setFill(Color.WHITE);

            label = new Label("🐾");
            label.setStyle("-fx-font-size: 30px; -fx-text-fill: #cbd5e1;");

            this.getChildren().addAll(back, label);

            this.setOnMouseClicked(e -> flip());
        }

        public void flip() {
            if (isAnimating || isFlipped || isMatched) return;

            isAnimating = true;
            RotateTransition rot1 = new RotateTransition(Duration.millis(150), this);
            rot1.setAxis(Rotate.Y_AXIS);
            rot1.setFromAngle(0);
            rot1.setToAngle(90);

            rot1.setOnFinished(e -> {
                isFlipped = true;
                label.setText(emoji);
                label.setStyle("-fx-font-size: 40px;");
                back.setFill(Color.web("#f8fafc"));

                RotateTransition rot2 = new RotateTransition(Duration.millis(150), this);
                rot2.setAxis(Rotate.Y_AXIS);
                rot2.setFromAngle(90);
                rot2.setToAngle(0);
                rot2.setOnFinished(e2 -> {
                    isAnimating = false;
                    checkMatch(this);
                });
                rot2.play();
            });
            rot1.play();
        }

        public void flipBack() {
            isAnimating = true;
            RotateTransition rot1 = new RotateTransition(Duration.millis(150), this);
            rot1.setAxis(Rotate.Y_AXIS);
            rot1.setFromAngle(0);
            rot1.setToAngle(90);

            rot1.setOnFinished(e -> {
                isFlipped = false;
                label.setText("🐾");
                label.setStyle("-fx-font-size: 30px; -fx-text-fill: #cbd5e1;");
                back.setFill(Color.WHITE);

                RotateTransition rot2 = new RotateTransition(Duration.millis(150), this);
                rot2.setAxis(Rotate.Y_AXIS);
                rot2.setFromAngle(90);
                rot2.setToAngle(0);
                rot2.setOnFinished(e2 -> isAnimating = false);
                rot2.play();
            });
            rot1.play();
        }
    }

    private void checkMatch(Card card) {
        if (firstCard == null) {
            firstCard = card;
        } else {
            secondCard = card;
            if (firstCard.emoji.equals(secondCard.emoji)) {
                firstCard.isMatched = true;
                secondCard.isMatched = true;
                pairsFound++;
                firstCard = null;
                secondCard = null;
                if (pairsFound == EMOJIS.length) {
                    showWinModal();
                }
            } else {
                isAnimating = true; // Prevent further clicks during pause
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e -> {
                    if (firstCard != null) firstCard.flipBack();
                    if (secondCard != null) secondCard.flipBack();
                    firstCard = null;
                    secondCard = null;
                    isAnimating = false;
                });
                pause.play();
            }
        }
    }

    private void showWinModal() {
        Stage parentStage = (Stage) this.getScene().getWindow();
        
        Stage modal = new Stage(StageStyle.TRANSPARENT);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(parentStage);

        // Close the game window immediately when winning
        if (parentStage != null) {
            parentStage.close();
        }

        VBox winLayout = new VBox(20);
        winLayout.setAlignment(Pos.CENTER);
        winLayout.setPadding(new Insets(40));
        winLayout.setStyle("-fx-background-color: white; -fx-background-radius: 40; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 40; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 10);");

        Label winTitle = new Label("WOW Congratulations!");
        winTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        winTitle.setScaleX(0); winTitle.setScaleY(0); // Start small

        Label winSub = new Label("You matched everything perfectly!");
        winSub.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        winSub.setScaleX(0); winSub.setScaleY(0); // Start small

        Button continueBtn = new Button("Continue ✨");
        continueBtn.setStyle("-fx-background-color: #d81b60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 12 60; -fx-cursor: hand;");
        continueBtn.setScaleX(0); continueBtn.setScaleY(0); // Start small
        
        continueBtn.setOnAction(e -> {
            modal.close();
            showQuoteModal();
        });

        winLayout.getChildren().addAll(winTitle, winSub, continueBtn);
        
        Scene scene = new Scene(winLayout);
        scene.setFill(Color.TRANSPARENT);
        modal.setScene(scene);
        
        // Sequential "Pop" animations
        modal.setOnShown(e -> {
            // 1. Title Pop
            javafx.animation.ScaleTransition st1 = new javafx.animation.ScaleTransition(Duration.millis(400), winTitle);
            st1.setToX(1.0); st1.setToY(1.0);
            st1.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            
            // 2. Subtitle Pop (after 300ms)
            javafx.animation.ScaleTransition st2 = new javafx.animation.ScaleTransition(Duration.millis(400), winSub);
            st2.setToX(1.0); st2.setToY(1.0);
            st2.setDelay(Duration.millis(300));
            st2.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            
            // 3. Button Pop (after 600ms)
            javafx.animation.ScaleTransition st3 = new javafx.animation.ScaleTransition(Duration.millis(400), continueBtn);
            st3.setToX(1.0); st3.setToY(1.0);
            st3.setDelay(Duration.millis(600));
            st3.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
            
            st1.play(); st2.play(); st3.play();
        });

        modal.show();
    }

    private void showQuoteModal() {
        Stage modal = new Stage(StageStyle.TRANSPARENT);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.initOwner(this.getScene().getWindow());

        VBox quoteLayout = new VBox(30);
        quoteLayout.setAlignment(Pos.CENTER);
        quoteLayout.setPadding(new Insets(40));
        quoteLayout.setStyle("-fx-background-color: white; -fx-background-radius: 40; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 40; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 10);");

        Label quoteLabel = new Label("\"" + quoteService.getRandomQuote() + "\"");
        quoteLabel.setWrapText(true);
        quoteLabel.setAlignment(Pos.CENTER);
        quoteLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #334155; -fx-font-style: italic;");
        quoteLabel.setScaleX(0); quoteLabel.setScaleY(0);

        HBox btns = new HBox(20);
        btns.setAlignment(Pos.CENTER);
        btns.setScaleX(0); btns.setScaleY(0);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #78909c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 12 40; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> {
            modal.close();
            if (onComplete != null) onComplete.run();
        });

        Button playAgainBtn = new Button("Play Again 🎮");
        playAgainBtn.setStyle("-fx-background-color: #d81b60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 12 40; -fx-cursor: hand;");
        playAgainBtn.setOnAction(e -> {
            modal.close();
            // Re-launch since the original window was closed
            CatMatchGame.launch(null, onComplete); 
        });

        btns.getChildren().addAll(cancelBtn, playAgainBtn);
        quoteLayout.getChildren().addAll(quoteLabel, btns);

        Scene scene = new Scene(quoteLayout);
        scene.setFill(Color.TRANSPARENT);
        modal.setScene(scene);

        modal.setOnShown(e -> {
            javafx.animation.ScaleTransition st1 = new javafx.animation.ScaleTransition(Duration.millis(400), quoteLabel);
            st1.setToX(1.0); st1.setToY(1.0);
            st1.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

            javafx.animation.ScaleTransition st2 = new javafx.animation.ScaleTransition(Duration.millis(400), btns);
            st2.setToX(1.0); st2.setToY(1.0);
            st2.setDelay(Duration.millis(300));
            st2.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

            st1.play(); st2.play();
        });

        modal.show();
    }

    private void resetGame() {
        this.getChildren().clear();
        pairsFound = 0;
        firstCard = null;
        secondCard = null;
        setupUI();
    }

    public static void launch(javafx.stage.Window owner, Runnable onComplete) {
        Stage gameStage = new Stage();
        gameStage.initModality(Modality.APPLICATION_MODAL);
        gameStage.initOwner(owner);
        gameStage.setTitle("Cat-tastic Match");

        CatMatchGame game = new CatMatchGame(() -> {
            gameStage.close();
            if (onComplete != null) onComplete.run();
        });
        
        Scene scene = new Scene(game, 600, 500);
        gameStage.setScene(scene);
        gameStage.show();
    }
}
