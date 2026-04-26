package gui.UsersControllers;

import entities.Group;
import entities.Users;
import javafx.fxml.FXML;
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

public class UserGroupsDashboardController {

    @FXML private ComboBox<Group> comboGroups;
    @FXML private VBox membersList;
    @FXML private Label lblTeamName;

    private final GroupService groupService = new GroupService();

    @FXML
    public void initialize() {
        Users currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        try {
            List<Group> joinedGroups = groupService.getGroupsForMember(currentUser.getId());
            
            // Set up ComboBox
            comboGroups.getItems().addAll(joinedGroups);
            comboGroups.setCellFactory(lv -> new ListCell<Group>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });
            comboGroups.setButtonCell(new ListCell<Group>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getName());
                }
            });

            comboGroups.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadGroupDetails(newVal);
                }
            });

            if (!joinedGroups.isEmpty()) {
                comboGroups.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGroupDetails(Group group) {
        lblTeamName.setText(group.getName());
        membersList.getChildren().clear();

        try {
            // 1. Get Owner
            Users owner = groupService.getGroupOwner(group.getCreatorId());
            if (owner != null) {
                membersList.getChildren().add(createMemberRow(owner, true));
            }

            // 2. Get Members
            List<Users> members = groupService.getGroupMembers(group.getId());
            for (Users member : members) {
                // Avoid duplicating the owner if they are also in the group_user table
                if (owner == null || member.getId() != owner.getId()) {
                    membersList.getChildren().add(createMemberRow(member, false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createMemberRow(Users member, boolean isOwner) {
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
        Label nameLabel = new Label(member.getUsername() + (isOwner ? " (Owner)" : ""));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        Label emailLabel = new Label(member.getEmail());
        emailLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        textInfo.getChildren().addAll(nameLabel, emailLabel);
        
        userInfo.getChildren().addAll(avatar, textInfo);

        // Role
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefWidth(180);
        Label roleLabel = new Label(isOwner ? "OWNER" : member.getRole());
        String badgeColor = isOwner ? "#ce2d7c" : (member.getRole().contains("MANAGER") ? "#4f46e5" : "#10b981");
        roleLabel.setStyle("-fx-background-color: " + badgeColor + "20; -fx-text-fill: " + badgeColor + "; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;");
        roleBadge.getChildren().add(roleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(userInfo, spacer, roleBadge);
        return row;
    }
}
