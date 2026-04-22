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
import javafx.stage.Stage;
import java.sql.SQLException;

public class AddMissionController {
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dateMission;
    @FXML private ComboBox<Idea> comboIdea;
    @FXML private Label lblMessage;

    private final MissionService missionService = new MissionService();
    private final IdeaService ideaService = new IdeaService();

    @FXML
    public void initialize() {
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void saveMission() {
        if (txtTitle.getText().isEmpty() || dateMission.getValue() == null || comboIdea.getValue() == null) {
            lblMessage.setText("❌ Title, Date and Idea selection are required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Mission mission = new Mission();
            mission.setTitle(txtTitle.getText().trim());
            mission.setDescription(txtDescription.getText().trim());
            mission.setMission_date(dateMission.getValue().toString());
            mission.setImplement_idea_id(comboIdea.getValue().getId());

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
        dateMission.setValue(null);
        comboIdea.getSelectionModel().clearSelection();
    }

    @FXML
    public void goBack() throws Exception {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/TSK/Mission.fxml"))));
    }
}
