package gui.UsersControllers;

import entities.Group;
import entities.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import services.GroupService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class GroupManagementController {

    @FXML
    private VBox membersList;
    @FXML
    private Label lblTeamName;

    private final GroupService groupService = new GroupService();
    private Group currentGroup;

    @FXML
    public void initialize() {
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null)
            return;

        try {
            currentGroup = groupService.getGroupByCreatorId(currentUser.getId());
            if (currentGroup == null) {
                // Create a default group for the creator if none exists
                currentGroup = new Group();
                currentGroup.setName(currentUser.getUsername() + "'s Team");
                currentGroup.setCreatorId(currentUser.getId());
                groupService.createGroup(currentGroup);
            }
            lblTeamName.setText(currentGroup.getName());
            loadMembers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMembers() {
        if (membersList == null)
            return;
        membersList.getChildren().clear();

        try {
            List<Users> members = groupService.getGroupMembers(currentGroup.getId());
            for (Users member : members) {
                membersList.getChildren().add(createMemberRow(member));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createMemberRow(Users member) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("table-row");
        row.setStyle("-fx-padding: 15 20; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // User Info
        HBox userInfo = new HBox(12);
        userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        userInfo.setPrefWidth(300);

        ImageView avatar = new ImageView();
        avatar.setFitHeight(40);
        avatar.setFitWidth(40);
        Circle clip = new Circle(20, 20, 20);
        avatar.setClip(clip);

        String imgUrl = member.getImage();
        if (imgUrl == null || imgUrl.isEmpty()) {
            imgUrl = "https://api.dicebear.com/7.x/avataaars/png?seed=" + member.getUsername();
        }
        avatar.setImage(new Image(imgUrl, true));

        VBox textInfo = new VBox(2);
        Label nameLabel = new Label(member.getUsername());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        Label emailLabel = new Label(member.getEmail());
        emailLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        textInfo.getChildren().addAll(nameLabel, emailLabel);

        userInfo.getChildren().addAll(avatar, textInfo);

        // Role
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefWidth(180);
        Label roleLabel = new Label(member.getRole());
        String badgeColor = member.getRole().contains("MANAGER") ? "#4f46e5" : "#10b981";
        roleLabel.setStyle("-fx-background-color: " + badgeColor + "20; -fx-text-fill: " + badgeColor
                + "; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
        roleBadge.getChildren().add(roleLabel);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(javafx.geometry.Pos.CENTER);
        actions.setPrefWidth(200);

        Button btnChangeRole = new Button("🔄 Role");
        btnChangeRole.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-cursor: hand;");
        btnChangeRole.setOnAction(e -> toggleRole(member));

        Button btnRemove = new Button("🗑 Delete");
        btnRemove.setStyle(
                "-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRemove.setOnAction(e -> removeMember(member));

        actions.getChildren().addAll(btnChangeRole, btnRemove);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        row.getChildren().addAll(userInfo, spacer1, roleBadge, spacer2, actions);
        return row;
    }

    private void toggleRole(Users member) {
        String newRole = member.getRole().equals("ROLE_MANAGER") ? "ROLE_MEMBER" : "ROLE_MANAGER";
        try {
            groupService.updateMemberRole(member.getId(), newRole);
            loadMembers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeMember(Users member) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Member");
        alert.setHeaderText("Remove " + member.getUsername() + " from your team?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                groupService.removeMemberFromGroup(member.getId(), currentGroup.getId());
                loadMembers();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAddMember() {
        try {
            StackPane contentArea = (StackPane) membersList.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/AddGroupMember.fxml"));
                Node root = loader.load();
                AddGroupMemberController controller = loader.getController();

                // Pass 0 if group doesn't exist yet; the AddMember controller will handle
                // creation
                int gId = (currentGroup != null) ? currentGroup.getId() : 0;
                controller.setGroupId(gId);

                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
