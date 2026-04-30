package gui;

import animatefx.animation.FadeInUp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Scene;
import services.UserCourseProgressService;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class LeaderboardController {

    @FXML private VBox firstPlaceCard, secondPlaceCard, thirdPlaceCard;
    @FXML private Label nameFirst, nameSecond, nameThird;
    @FXML private Label openedFirst, openedSecond, openedThird;
    @FXML private ImageView imgFirst, imgSecond, imgThird;
    @FXML private ImageView badgeFirst, badgeSecond, badgeThird;
    @FXML private HBox categoryLeadersContainer;
    @FXML private VBox rankingContainer;

    private final UserCourseProgressService progressService = new UserCourseProgressService();

    @FXML
    public void initialize() {
        new FadeInUp(firstPlaceCard).play();
        new FadeInUp(secondPlaceCard).play();
        new FadeInUp(thirdPlaceCard).play();
        
        loadTopThree();
        loadCategoryLeaders();
        loadGlobalRankings();
    }

    private void loadTopThree() {
        try {
            List<Map<String, Object>> top3 = progressService.getGlobalRankings(3);
            if (top3.size() >= 1) populateTopCard(top3.get(0), nameFirst, openedFirst, imgFirst);
            if (top3.size() >= 2) populateTopCard(top3.get(1), nameSecond, openedSecond, imgSecond);
            if (top3.size() >= 3) populateTopCard(top3.get(2), nameThird, openedThird, imgThird);
            
            // Load badges
            loadBadge(badgeFirst, "first.png");
            loadBadge(badgeSecond, "second.png");
            loadBadge(badgeThird, "third.png");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void populateTopCard(Map<String, Object> data, Label nameLbl, Label countLbl, ImageView img) {
        String name = (String) data.get("name");
        nameLbl.setText(name != null ? name : "Unknown User");
        nameLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"); // Ensure dark color
        
        countLbl.setText(data.get("count") + " Resources");
        
        String imgPath = (String) data.get("image");
        loadAvatar(img, imgPath, name);
    }

    private void setDefaultAvatar(ImageView img, String userName) {
        String safeName = (userName != null && !userName.isBlank()) ? userName : "User";
        String encodedName = safeName.replace(" ", "+");
        img.setImage(new Image("https://ui-avatars.com/api/?name=" + encodedName + "&background=ce2d7c&color=fff", true));
    }

    private void loadAvatar(ImageView img, String imagePath, String userName) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                Image image;
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file:")) {
                    image = new Image(imagePath, true);
                } else {
                    java.io.File file = new java.io.File(imagePath);
                    if (!file.exists()) {
                        file = new java.io.File("src/main/resources/uploads/images/" + imagePath);
                    }
                    if (file.exists()) {
                        image = new Image(file.toURI().toString(), true);
                    } else {
                        image = new Image(imagePath, true);
                    }
                }
                img.setImage(image);
                return;
            } catch (Exception e) {
                // fall through to default avatar
            }
        }
        setDefaultAvatar(img, userName);
    }

    private void loadBadge(ImageView badgeView, String badgeFileName) {
        try {
            Image badge = new Image(getClass().getResourceAsStream("/gui/images/badges/" + badgeFileName));
            badgeView.setImage(badge);
        } catch (Exception e) {
            // Badge not found or error loading
            System.err.println("Failed to load badge: " + badgeFileName);
        }
    }

    private void loadCategoryLeaders() {
        try {
            List<Map<String, Object>> leaders = progressService.getCategoryLeaders(4);
            categoryLeadersContainer.getChildren().clear();
            for (Map<String, Object> leader : leaders) {
                categoryLeadersContainer.getChildren().add(createCategoryLeaderCard(leader));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox createCategoryLeaderCard(Map<String, Object> data) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setStyle("-fx-padding: 15; -fx-alignment: CENTER;");

        Label catLabel = new Label(((String) data.get("categoryName")).toUpperCase());
        catLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 9px;");

        ImageView img = new ImageView();
        img.setFitHeight(50);
        img.setFitWidth(50);
        img.setClip(new Circle(25, 25, 25));
        String imgPath = (String) data.get("userImage");
        String userName = (String) data.get("userName");
        loadAvatar(img, imgPath, userName);
        Label name = new Label(userName != null ? userName : "Unknown");
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label count = new Label(data.get("count") + " pts");
        count.setStyle("-fx-text-fill: -fx-primary-pink; -fx-font-weight: bold;");

        card.getChildren().addAll(catLabel, img, name, count);
        return card;
    }

    private void loadGlobalRankings() {
        try {
            List<Map<String, Object>> rankings = progressService.getGlobalRankings(0);
            int rank = 1;
            for (Map<String, Object> user : rankings) {
                rankingContainer.getChildren().add(createRankingRow(rank++, user));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox createRankingRow(int rank, Map<String, Object> data) {
        HBox row = new HBox(20);
        row.getStyleClass().add("list-row");
        row.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10 25;");

        Label rankLbl = new Label("#" + rank);
        rankLbl.setPrefWidth(60);
        rankLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + (rank <= 3 ? "#fbbf24" : "#64748b") + ";");

        HBox userBox = new HBox(15);
        userBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(userBox, javafx.scene.layout.Priority.ALWAYS);
        
        ImageView img = new ImageView();
        img.setFitHeight(35);
        img.setFitWidth(35);
        img.setClip(new Circle(17.5, 17.5, 17.5));
        String imgPath = (String) data.get("image");
        String userName = (String) data.get("name");
        loadAvatar(img, imgPath, userName);
        Label name = new Label(userName != null ? userName : "Unknown User");
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3748;");
        userBox.getChildren().addAll(img, name);

        Label count = new Label(data.get("count") + " Resources");
        count.setPrefWidth(150);
        count.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        count.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-primary-pink;");

        row.getChildren().addAll(rankLbl, userBox, count);
        return row;
    }

    @FXML
    private void onBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/gui/front-courses-grid-view.fxml"));
            Stage stage = (Stage) rankingContainer.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
