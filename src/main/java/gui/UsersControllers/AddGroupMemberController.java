package gui.UsersControllers;

import entities.Users;
import entities.Group;
import services.UsersService;
import services.GroupService;
import utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddGroupMemberController {

    @FXML private TextField txtUsername, txtEmail, txtNumtel;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnTogglePassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private Label lblMessage;

    private final services.NotificationService notificationService = new services.NotificationService();
    private final UsersService usersService = new UsersService();
    private final GroupService groupService = new GroupService();
    private int groupId;
    @FXML private ComboBox<Users> comboInviteUser;
    @FXML private javafx.scene.control.TabPane tabPane;

    @FXML
    public void initialize() {
        comboRole.getItems().addAll("ROLE_MANAGER", "ROLE_MEMBER");
        comboRole.setValue("ROLE_MEMBER");
    }

    private void setupInviteUserCombo() {
        try {
            java.util.List<String> roles = java.util.Arrays.asList("ROLE_MEMBER", "ROLE_MANAGER");
            java.util.List<Users> users = usersService.findByRoles(roles);
            
            // Filter out existing members
            if (groupId != 0) {
                java.util.List<Users> existingMembers = groupService.getGroupMembers(groupId);
                java.util.Set<Integer> existingIds = existingMembers.stream().map(Users::getId).collect(java.util.stream.Collectors.toSet());
                users = users.stream().filter(u -> !existingIds.contains(u.getId())).collect(java.util.stream.Collectors.toList());
            }

            comboInviteUser.setItems(javafx.collections.FXCollections.observableArrayList(users));
            
            // Custom cell factory to show username and email
            comboInviteUser.setCellFactory(lv -> new ListCell<Users>() {
                @Override
                protected void updateItem(Users item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getUsername() + " (" + item.getEmail() + ")");
                    }
                }
            });
            
            comboInviteUser.setButtonCell(new ListCell<Users>() {
                @Override
                protected void updateItem(Users item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getUsername() + " (" + item.getEmail() + ")");
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSendInvitation() {
        Users selectedUser = comboInviteUser.getValue();
        if (selectedUser == null) {
            lblMessage.setStyle("-fx-text-fill: #e53e3e;");
            lblMessage.setText("❌ Please select a user to invite.");
            return;
        }

        try {
            Users currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            // Ensure group exists
            if (groupId == 0) {
                Group g = groupService.getGroupByCreatorId(currentUser.getId());
                if (g == null) {
                    g = new Group();
                    g.setName(currentUser.getUsername() + "'s Team");
                    g.setCreatorId(currentUser.getId());
                    groupService.createGroup(g);
                }
                groupId = g.getId();
            }

            // Send Notification
            notificationService.notifyGroupInvitation(selectedUser.getId(), currentUser.getUsername(), groupId);

            lblMessage.setStyle("-fx-text-fill: #38a169;");
            lblMessage.setText("✅ Invitation sent to " + selectedUser.getUsername() + "!");

            // Reset selection and refresh list
            comboInviteUser.setValue(null);
            setupInviteUserCombo();

        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: #e53e3e;");
            lblMessage.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
        setupInviteUserCombo(); // Refresh filtering now that we have the ID
    }

    private String validate() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText();
        String numtel = txtNumtel.getText().trim();

        if (username.length() < 4) return "❌ Username must be at least 4 characters.";
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) return "❌ Invalid email address.";
        if (password.length() < 8) return "❌ Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*")) return "❌ Password must contain at least 1 uppercase letter.";
        if (!password.matches(".*[0-9].*")) return "❌ Password must contain at least 1 number.";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) return "❌ Password must contain at least 1 special character.";
        if (!numtel.isEmpty() && !numtel.matches("^\\d{8}$")) return "❌ Phone number must be exactly 8 digits.";

        return null;
    }

    @FXML
    public void saveMember() {
        String error = validate();
        if (error != null) {
            lblMessage.setStyle("-fx-text-fill: #e53e3e;");
            lblMessage.setText(error);
            return;
        }

        try {
            Users currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            // 1. Create the Group if it doesn't exist (Lazy Creation)
            if (groupId == 0) {
                Group g = groupService.getGroupByCreatorId(currentUser.getId());
                if (g == null) {
                    g = new Group();
                    g.setName(currentUser.getUsername() + "'s Team");
                    g.setCreatorId(currentUser.getId()); // Map to owner_id in service
                    groupService.createGroup(g);
                }
                groupId = g.getId();
            }

            // 2. Create the User
            Users u = new Users();
            u.setUsername(txtUsername.getText().trim());
            u.setEmail(txtEmail.getText().trim());
            u.setPassword(txtPassword.isVisible() ? txtPassword.getText() : txtPasswordVisible.getText());
            u.setRole(comboRole.getValue()); // Role stored in users table
            u.setNumtel(txtNumtel.getText().trim());
            u.setPoints(0);
            u.setCreated_at(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            usersService.ajouter(u);
            
            // 3. Fetch the created user to get the ID
            Users createdUser = usersService.findByEmail(u.getEmail());
            
            if (createdUser != null) {
                // 4. Add to the group_user join table
                groupService.addMemberToGroup(createdUser.getId(), groupId);
                
                lblMessage.setStyle("-fx-text-fill: #38a169;");
                lblMessage.setText("✅ Member added successfully!");
                
                // Clear fields
                txtUsername.clear();
                txtEmail.clear();
                txtPassword.clear();
                txtPasswordVisible.clear();
                txtNumtel.clear();
                setupInviteUserCombo(); // Refresh the invite list just in case
            }
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: #e53e3e;");
            lblMessage.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void togglePassword() {
        if (txtPassword.isVisible()) {
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            btnTogglePassword.setText("🙈");
        } else {
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    public void goBack() {
        try {
            StackPane contentArea = (StackPane) txtUsername.getScene().lookup("#contentArea");
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Users/GroupManagement.fxml"));
                contentArea.getChildren().setAll((Node) loader.load());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
