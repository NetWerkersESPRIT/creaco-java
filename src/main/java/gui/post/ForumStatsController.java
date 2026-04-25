package gui.post;

import entities.Post;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import services.forum.ForumStatsService;

import java.sql.SQLException;
import java.util.List;

public class ForumStatsController {

    @FXML private Label topInfluencerName;
    @FXML private Label topInfluencerStats;
    @FXML private Label mostLikedTitle;
    @FXML private Label mostLikedCount;
    @FXML private Label mostDiscussedTitle;
    @FXML private Label mostDiscussedCount;

    @FXML private BarChart<String, Number> activityChart;
    @FXML private PieChart contentPieChart;
    @FXML private VBox topPostsList;
    @FXML private VBox topCommentedList;

    private final ForumStatsService statsService = new ForumStatsService();

    @FXML
    public void initialize() {
        refreshStats();
    }

    @FXML
    private void refreshStats() {
        try {
            loadOverview();
            loadActivityChart();
            loadTopPosts();
            loadTopCommentedPosts();
            loadContentBreakdown();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOverview() throws SQLException {
        // Top Influencer
        List<ForumStatsService.UserActivity> topUsers = statsService.getMostActiveUsers(1);
        if (!topUsers.isEmpty()) {
            ForumStatsService.UserActivity top = topUsers.get(0);
            topInfluencerName.setText(top.username);
            topInfluencerStats.setText(top.totalActivity + " total activities (" + top.postCount + " posts, " + top.commentCount + " comments)");
        }

        // Most Liked Post
        List<Post> topLiked = statsService.getTopLikedPosts(1);
        if (!topLiked.isEmpty()) {
            Post top = topLiked.get(0);
            mostLikedTitle.setText(top.getTitle());
            mostLikedCount.setText(top.getLikes() + " likes");
        }

        // Most Discussed Post
        List<ForumStatsService.PostWithCommentCount> topDiscussed = statsService.getTopCommentedPosts(1);
        if (!topDiscussed.isEmpty()) {
            ForumStatsService.PostWithCommentCount top = topDiscussed.get(0);
            mostDiscussedTitle.setText(top.post.getTitle());
            mostDiscussedCount.setText(top.commentCount + " comments");
        }
    }

    private void loadActivityChart() throws SQLException {
        activityChart.getData().clear();
        
        CategoryAxis xAxis = (CategoryAxis) activityChart.getXAxis();
        xAxis.getCategories().clear();
        
        List<ForumStatsService.UserActivity> activeUsers = statsService.getMostActiveUsers(8);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activity Score");
        
        for (ForumStatsService.UserActivity user : activeUsers) {
            // Explicitly add to categories to ensure they appear on the axis
            if (!xAxis.getCategories().contains(user.username)) {
                xAxis.getCategories().add(user.username);
            }
            series.getData().add(new XYChart.Data<>(user.username, user.totalActivity));
        }
        
        activityChart.getData().add(series);
        
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickLabelRotation(0); // Set to 0 first to see if they fit
    }

    private void loadTopPosts() throws SQLException {
        topPostsList.getChildren().clear();
        List<Post> topLiked = statsService.getTopLikedPosts(3);
        
        for (Post p : topLiked) {
            topPostsList.getChildren().add(buildStatRow(p.getTitle(), "❤️ " + p.getLikes() + " likes", "#f43f5e"));
        }
    }

    private void loadTopCommentedPosts() throws SQLException {
        topCommentedList.getChildren().clear();
        List<ForumStatsService.PostWithCommentCount> topDiscussed = statsService.getTopCommentedPosts(3);
        
        for (ForumStatsService.PostWithCommentCount item : topDiscussed) {
            topCommentedList.getChildren().add(buildStatRow(item.post.getTitle(), "💬 " + item.commentCount + " comments", "#10b981"));
        }
    }

    private HBox buildStatRow(String titleStr, String statStr, String color) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 12;");
        
        VBox info = new VBox(2);
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label stat = new Label(statStr);
        stat.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");
        
        info.getChildren().addAll(title, stat);
        card.getChildren().add(info);
        return card;
    }

    private void loadContentBreakdown() throws SQLException {
        contentPieChart.getData().clear();
        int posts = statsService.getTotalPosts();
        int comments = statsService.getTotalComments();
        
        if (posts > 0 || comments > 0) {
            PieChart.Data pData = new PieChart.Data("Posts (" + posts + ")", posts);
            PieChart.Data cData = new PieChart.Data("Comments (" + comments + ")", comments);
            
            contentPieChart.getData().addAll(pData, cData);
            contentPieChart.setLabelsVisible(true);
            contentPieChart.setLegendVisible(false);
            contentPieChart.setStartAngle(90);
        }
    }
}
