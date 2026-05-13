package gui.TSKControllers;

import entities.Idea;
import entities.Mission;
import services.IdeaService;
import services.MissionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddMissionController {
    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dateMission;
    @FXML
    private TextField txtTime;
    @FXML
    private ComboBox<Idea> comboIdea;
    @FXML
    private Label lblNavUsername;
    @FXML
    private Label lblNavUserRole;
    @FXML
    private Label lblMessage;

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

    public void setInitialDate(java.time.LocalDate date) {
        if (dateMission != null) {
            dateMission.setValue(date);
        }
    }

    @FXML
    public void goToIdea() throws Exception {
        switchScene("/TSK/Idea.fxml");
    }

    @FXML
    public void goToMission() throws Exception {
        switchScene("/TSK/Mission.fxml");
    }

    @FXML
    public void goToTasks() throws Exception {
        switchScene("/TSK/Tasks.fxml");
    }

    @FXML
    public void goToAdmin() throws Exception {
        switchScene("/Users/Admin.fxml");
    }

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
        if (txtTitle.getText().isEmpty() || (dateMission != null && dateMission.getValue() == null)
                || (comboIdea != null && comboIdea.getValue() == null)) {
            lblMessage.setText("❌ Missing required fields.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        // Validate that the date/time is not in the past
        try {
            String timeStr = (txtTime != null && !txtTime.getText().isBlank()) ? txtTime.getText().trim() : "12:00";
            // Support both HH:mm and HH:mm:ss if needed, but default to HH:mm
            LocalTime time;
            try {
                time = LocalTime.parse(timeStr);
            } catch (DateTimeParseException e) {
                // Try fallback if user entered something slightly different, or just fail
                lblMessage.setText("❌ Invalid time format. Use HH:mm");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }

            LocalDateTime selectedDateTime = LocalDateTime.of(dateMission.getValue(), time);
            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                lblMessage.setText("❌ Cannot create a mission in the past.");
                lblMessage.setStyle("-fx-text-fill: red;");
                return;
            }
        } catch (Exception e) {
            lblMessage.setText("❌ Invalid date/time selected.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Mission mission = new Mission();
            mission.setTitle(txtTitle.getText().trim());
            mission.setDescription(txtDescription.getText().trim());
            if (dateMission != null && dateMission.getValue() != null) {
                String time = (txtTime != null && !txtTime.getText().isBlank()) ? txtTime.getText().trim() : "12:00";
                mission.setMission_datetime(dateMission.getValue().toString() + " " + time);
            }
            if (comboIdea != null)
                mission.setImplement_idea_id(comboIdea.getValue().getId());

            entities.Users currentUser = utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                mission.setAssigned_by_id(currentUser.getId());
            } else {
                mission.setAssigned_by_id(1); // Fallback
            }

            missionService.ajouter(mission);
            
            gui.util.AlertHelper.showCustomAlert(
                "Mission Created", 
                "Mission added successfully!", 
                gui.util.AlertHelper.AlertType.INFORMATION
            );
            
            goToMission();
        } catch (SQLException e) {
            lblMessage.setText("❌ Error: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtTitle.clear();
        txtDescription.clear();
        if (dateMission != null)
            dateMission.setValue(null);
        if (comboIdea != null)
            comboIdea.getSelectionModel().clearSelection();
    }

    @FXML
    public void onOpenProfile(javafx.scene.input.MouseEvent event) {
        try {
            switchScene("/Users/Profile.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    public void onGenerateAI(javafx.event.ActionEvent event) {
        String title = txtTitle.getText().trim();

        if (title.isEmpty()) {
            lblMessage.setText("❌ Please enter a mission title first.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }

        String currentDescription = txtDescription.getText().trim();
        String prompt;

        if (currentDescription.isEmpty() || currentDescription.equals("Generating description...")) {
            prompt = String.format(
                    "You are a content creator who has a team of editors that work under him. Generate a short description that is of interest to your team mentioning the type of work they need to do to complete the mission, based on the provided idea, its description, its category and the name of the mission. MAX 1000 chars total. Emphasize on the tasks that need to be done. to complete the mission. Be precise and informative of the steps and requirements. Refrain from repeating the mission and idea titles and the idea's category.\""
                            + "Mission Title: '%s'." + "Keep it concise (max 2 sentences).",
                    title);
        } else {
            prompt = String.format(
                    "You are a content creator who has a team of editors that work under him. Generate a short description that is of interest to your team mentioning the type of work they need to do to complete the mission, based on the provided idea, its description, its category and the name and current description of the mission. MAX 1000 chars total. Emphasize on the tasks that need to be done. to complete the mission. Be precise and informative of the steps and requirements. Refrain from repeating the mission and idea titles and the idea's category.\""
                            + "Mission Title: '%s', Current Description: '%s'" + "Keep it concise (max 2 sentences).",
                    title, currentDescription);
        }

        txtDescription.setText("Generating description...");

        new Thread(() -> {
            String response = utils.GeminiService.getGeminiResponse(prompt);
            javafx.application.Platform.runLater(() -> {
                txtDescription.setText(response);
            });
        }).start();
    }

    @FXML
    public void goBack() throws Exception {
        goToMission();
    }
}
