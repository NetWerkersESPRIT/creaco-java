package gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import services.UserCourseProgressService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LeaderboardController {

    private final UserCourseProgressService progressService = new UserCourseProgressService();

    @FXML private VBox firstPlaceCard;
    @FXML private VBox secondPlaceCard;
    @FXML private VBox thirdPlaceCard;

    @FXML private ImageView imgFirst;
    @FXML private ImageView imgSecond;
    @FXML private ImageView imgThird;

    @FXML private Label nameFirst;
    @FXML private Label nameSecond;
    @FXML private Label nameThird;

    @FXML private Label openedFirst;
    @FXML private Label openedSecond;
    @FXML private Label openedThird;

    @FXML private ImageView badgeFirst;
    @FXML private ImageView badgeSecond;
    @FXML private ImageView badgeThird;

    @FXML private HBox categoryLeadersContainer;
    @FXML private VBox rankingContainer;

    @FXML
    public void initialize() {
        loadGlobalRankings();
        loadCategoryLeaders();
    }

    private void loadGlobalRankings() {
        try {
            List<Map<String, Object>> rankings = progressService.getGlobalRankings(10);
            
            // Populate Top 3
            if (rankings.size() >= 1) populateTopCard(rankings.get(0), nameFirst, openedFirst, imgFirst, badgeFirst, "/gui/assets/gold-medal.png");
            if (rankings.size() >= 2) populateTopCard(rankings.get(1), nameSecond, openedSecond, imgSecond, badgeSecond, "/gui/assets/silver-medal.png");
            if (rankings.size() >= 3) populateTopCard(rankings.get(2), nameThird, openedThird, imgThird, badgeThird, "/gui/assets/bronze-medal.png");

            // Populate Table (excluding top 3 or showing all)
            for (int i = 0; i < rankings.size(); i++) {
                rankingContainer.getChildren().add(buildRankingRow(i + 1, rankings.get(i)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTopCard(Map<String, Object> data, Label nameLbl, Label countLbl, ImageView imgView, ImageView badgeView, String badgePath) {
        nameLbl.setText((String) data.get("name"));
        countLbl.setText(data.get("count") + " Resources");
        
        String imgUrl = (String) data.get("image");
        String username = (String) data.get("name");
        imgView.setImage(new Image(getProcessedAvatarUrl(imgUrl, username), true));
        
        try {
            badgeView.setImage(new Image(getClass().getResourceAsStream(badgePath)));
        } catch (Exception e) { /* badge might be missing */ }
    }

    private Node buildRankingRow(int rank, Map<String, Object> data) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 25; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        Label rankLbl = new Label("#" + rank);
        rankLbl.setPrefWidth(60);
        rankLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (rank <= 3 ? "#ec4899" : "#64748b") + "; -fx-font-size: 14px;");

        HBox userBox = new HBox(15);
        userBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        StackPane imgContainer = new StackPane();
        Circle clip = new Circle(18, 18, 18);
        ImageView userImg = new ImageView();
        userImg.setFitHeight(36);
        userImg.setFitWidth(36);
        userImg.setClip(clip);
        String url = (String) data.get("image");
        String username = (String) data.get("name");
        userImg.setImage(new Image(getProcessedAvatarUrl(url, username), true));
        imgContainer.getChildren().add(userImg);

        Label nameLbl = new Label((String) data.get("name"));
        nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        userBox.getChildren().addAll(imgContainer, nameLbl);

        Label countLbl = new Label(data.get("count") + " Resources");
        countLbl.setPrefWidth(150);
        countLbl.setAlignment(Pos.CENTER_RIGHT);
        countLbl.setStyle("-fx-text-fill: #64748b; -fx-font-weight: 500;");

        row.getChildren().addAll(rankLbl, userBox, countLbl);
        return row;
    }

    private void loadCategoryLeaders() {
        if (categoryLeadersContainer == null) return;
        try {
            List<Map<String, Object>> leaders = progressService.getCategoryLeaders(4);
            categoryLeadersContainer.getChildren().clear();
            for (Map<String, Object> leader : leaders) {
                categoryLeadersContainer.getChildren().add(buildCategoryLeaderCard(leader));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node buildCategoryLeaderCard(Map<String, Object> data) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-padding: 20; -fx-background-color: #f8fafc;");

        Label catLbl = new Label(((String) data.get("categoryName")).toUpperCase());
        catLbl.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold; -fx-font-size: 10px;");

        Circle clip = new Circle(25, 25, 25);
        ImageView userImg = new ImageView();
        userImg.setFitWidth(50);
        userImg.setFitHeight(50);
        userImg.setClip(clip);
        String url = (String) data.get("userImage");
        String username = (String) data.get("userName");
        userImg.setImage(new Image(getProcessedAvatarUrl(url, username), true));

        Label nameLbl = new Label((String) data.get("userName"));
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label scoreLbl = new Label(data.get("count") + " Resources");
        scoreLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        card.getChildren().addAll(catLbl, userImg, nameLbl, scoreLbl);
        return card;
    }

    private String getProcessedAvatarUrl(String rawUrl, String username) {
        if (rawUrl != null && !rawUrl.trim().isEmpty()) {
            if (rawUrl.contains("dicebear.com") && rawUrl.contains("/svg")) {
                return rawUrl.replace("/svg", "/png");
            }
            return rawUrl;
        }
        return utils.DiceBearService.generateInitialsUrl(username != null ? username : "User");
    }

    @FXML
    private void onBack() {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onShowCourses();
        }
    }
}
