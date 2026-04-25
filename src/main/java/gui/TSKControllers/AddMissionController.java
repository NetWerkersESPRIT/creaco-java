package gui.TSKControllers;

import entities.Idea;
import entities.Mission;
import services.IdeaService;
import services.MissionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddMissionController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateMission;
    @FXML private ComboBox<Idea> comboIdea;
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;
    @FXML private Label lblMessage;

    private final MissionService missionService = new MissionService();
    private final IdeaService ideaService = new IdeaService();

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Create Mission", "Missions / New");
        try {
            if (comboIdea != null) {
                // Load ideas into the combo box
                comboIdea.setItems(FXCollections.observableArrayList(ideaService.afficher()));
                
                // Customize how Ideas are displayed in the combo box
                comboIdea.setCellFactory(lv -> new ListCell<Idea>() {
                    @Override
                    protected void updateItem(Idea item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getTitle());
                    }
                });
                comboIdea.setButtonCell(new ListCell<Idea>() {
                    @Override
                    protected void updateItem(Idea item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getTitle());
                    }
                });
            }

            // Populate Navbar Profile
            entities.Users current = utils.SessionManager.getInstance().getCurrentUser();
            if (current != null && lblNavUsername != null) {
                lblNavUsername.setText(current.getUsername());
                String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
                lblNavUserRole.setText(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }
    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) txtTitle.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;

        if (root instanceof BorderPane) {
            view = ((BorderPane) root).getCenter();
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(view);
        } else {
            Stage stage = (Stage) txtTitle.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void saveMission() {
        if (txtTitle.getText().isEmpty() || (dateMission != null && dateMission.getValue() == null) || (comboIdea != null && comboIdea.getValue() == null)) {
            lblMessage.setText("❌ Missing required fields.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Mission mission = new Mission();
            mission.setTitle(txtTitle.getText().trim());
            mission.setDescription(txtDescription.getText().trim());
            if (dateMission != null) mission.setMission_date(dateMission.getValue().toString());
            if (comboIdea != null) mission.setImplement_idea_id(comboIdea.getValue().getId());

            missionService.ajouter(mission);
            lblMessage.setText("✅ Mission added successfully!");
            lblMessage.setStyle("-fx-text-fill: green;");
            clearFields();
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void clearFields() {
        txtTitle.clear();
        txtDescription.clear();
        if (dateMission != null) dateMission.setValue(null);
        if (comboIdea != null) comboIdea.getSelectionModel().clearSelection();
    }

    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try { switchScene("/Users/Profile.fxml"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    public void goBack() throws Exception {
        goToMission();
    }
}
