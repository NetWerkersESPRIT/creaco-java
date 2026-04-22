package gui.TSKControllers;

import services.IdeaService;
import utils.SessionManager;
import entities.Idea;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class IdeaController {
    @FXML private javafx.scene.control.Button btnAdmin;
    @FXML private TableView<Idea> ideaTable;
    @FXML private TableColumn<Idea, Integer> colId;
    @FXML private TableColumn<Idea, String> colTitle;
    @FXML private TableColumn<Idea, String> colDescription;
    @FXML private TableColumn<Idea, String> colCategory;
    @FXML private TableColumn<Idea, Void> colActions;

    private final IdeaService ideaService = new IdeaService();

    @FXML
    public void initialize() {
        // Hide Admin button if not admin
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (btnAdmin != null) {
            btnAdmin.setVisible(isAdmin);
            btnAdmin.setManaged(isAdmin);
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        addActionButtons();
        loadIdeas();
    }

    @FXML public void goToAdmin()   throws Exception { switchScene("/Users/Admin.fxml"); }
    @FXML public void goToIdea()    throws Exception { switchScene("/TSK/Idea.fxml"); }
    @FXML public void goToMission() throws Exception { switchScene("/TSK/Mission.fxml"); }
    @FXML public void goToTasks()   throws Exception { switchScene("/TSK/Tasks.fxml"); }

    private void switchScene(String fxml) throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) ideaTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource(fxml))));
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btnDelete = new javafx.scene.control.Button("🗑 Delete");
            {
                btnDelete.setOnAction(e -> {
                    Idea idea = getTableView().getItems().get(getIndex());
                    try {
                        ideaService.supprimer(idea.getId());
                        loadIdeas();
                    } catch (java.sql.SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    private void loadIdeas() {
        try {
            ideaTable.setItems(javafx.collections.FXCollections.observableArrayList(ideaService.afficher()));
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddIdea() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) ideaTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/TSK/AddIdea.fxml"))));
    }

    @FXML
    public void goBack() throws Exception {
        javafx.stage.Stage stage = (javafx.stage.Stage) ideaTable.getScene().getWindow();
        stage.setScene(new javafx.scene.Scene(javafx.fxml.FXMLLoader.load(getClass().getResource("/gui/front-main-view.fxml"))));
    }
}
