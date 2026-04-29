package gui.TSKControllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entities.Idea;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.IdeaService;
import utils.SessionManager;

public class IdeaController {
    @FXML
    private VBox ideasList;
    @FXML
    private Label lblNavUsername;
    @FXML
    private Label lblNavUserRole;

    // New UI refs
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private Button btnViewDiscover;
    @FXML
    private Button btnViewBrowse;
    @FXML
    private HBox searchBar;
    @FXML
    private HBox columnHeaders;

    private final IdeaService ideaService = new IdeaService();
    private List<Idea> allIdeas = new ArrayList<>();
    private boolean browseMode = false; // default: Discover view (same as non-admin platform)

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String BTN_ACTIVE = "-fx-background-color: white; -fx-text-fill: #cb0c9f; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.07),6,0,0,2); -fx-cursor: hand;";
    private static final String BTN_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Ideas Shelf", "Pages / Innovation / Ideas");

        // Populate Navbar Profile
        entities.Users current = SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            lblNavUsername.setText(current.getUsername());
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
        }

        loadIdeas();
        populateCategories();
        // Start in Discover view (hidden search bar/headers) — same as the platform for
        // non-admins
        applyView(false);
    }

    // ── View Switching ─────────────────────────────────────────────────────────
    @FXML
    public void switchToDiscover() {
        applyView(false);
    }

    @FXML
    public void switchToBrowse() {
        applyView(true);
    }

    private void applyView(boolean browse) {
        browseMode = browse;
        if (searchBar != null) {
            searchBar.setVisible(browse);
            searchBar.setManaged(browse);
        }
        if (columnHeaders != null) {
            columnHeaders.setVisible(browse);
            columnHeaders.setManaged(browse);
        }

        if (btnViewDiscover != null)
            btnViewDiscover.setStyle(browse ? BTN_INACTIVE : BTN_ACTIVE);
        if (btnViewBrowse != null)
            btnViewBrowse.setStyle(browse ? BTN_ACTIVE : BTN_INACTIVE);

        filterAndRender();
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadIdeas() {
        try {
            allIdeas = ideaService.afficher();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        filterAndRender();
    }

    private void populateCategories() {
        if (cmbCategory == null)
            return;
        List<String> cats = new ArrayList<>();
        cats.add("All Categories");
        for (Idea i : allIdeas) {
            String cat = i.getCategory();
            if (cat != null && !cat.isEmpty() && !cats.contains(cat))
                cats.add(cat);
        }
        cmbCategory.setItems(FXCollections.observableArrayList(cats));
        cmbCategory.getSelectionModel().selectFirst();
    }

    // ── Search / Filter ───────────────────────────────────────────────────────
    @FXML
    public void onSearch() {
        filterAndRender();
    }

    private void filterAndRender() {
        if (ideasList == null)
            return;
        ideasList.getChildren().clear();

        String search = (txtSearch != null) ? txtSearch.getText().toLowerCase().trim() : "";
        String category = (cmbCategory != null && cmbCategory.getValue() != null
                && !cmbCategory.getValue().equals("All Categories"))
                        ? cmbCategory.getValue().toLowerCase()
                        : "";

        List<Idea> filtered = new ArrayList<>();
        for (Idea i : allIdeas) {
            String title = i.getTitle() != null ? i.getTitle().toLowerCase() : "";
            String desc = i.getDescription() != null ? i.getDescription().toLowerCase() : "";
            String cat = i.getCategory() != null ? i.getCategory().toLowerCase() : "";

            boolean matchSearch = search.isEmpty() || title.contains(search) || desc.contains(search)
                    || cat.contains(search);
            boolean matchCategory = category.isEmpty() || cat.equals(category);

            if (matchSearch && matchCategory) {
                filtered.add(i);
            }
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("No ideas found.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 20;");
            ideasList.getChildren().add(empty);
            return;
        }

        if (browseMode) {
            for (Idea i : filtered) {
                ideasList.getChildren().add(buildTableRow(i));
            }
        } else {
            // Split into Trending and Recommended
            HBox discoverLayout = new HBox(40);

            VBox trendingCol = new VBox(15);
            HBox.setHgrow(trendingCol, Priority.ALWAYS);
            trendingCol.setPrefWidth(500);

            HBox trendingHeader = new HBox(12);
            trendingHeader.setAlignment(Pos.CENTER_LEFT);
            Label trendingIcon = new Label("🔥");
            trendingIcon.setStyle(
                    "-fx-font-size: 16px; -fx-text-fill: #f97316; -fx-background-color: #ffedd5; -fx-padding: 8 12; -fx-background-radius: 8;");
            VBox trendingTexts = new VBox(2);
            Label lblT1 = new Label("Trending Now");
            lblT1.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 14px;");
            Label lblT2 = new Label("MOST IMPLEMENTED");
            lblT2.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-font-size: 10px;");
            trendingTexts.getChildren().addAll(lblT1, lblT2);
            trendingHeader.getChildren().addAll(trendingIcon, trendingTexts);
            trendingCol.getChildren().add(trendingHeader);

            VBox recommendedCol = new VBox(15);
            HBox.setHgrow(recommendedCol, Priority.ALWAYS);
            recommendedCol.setPrefWidth(500);

            HBox recommendedHeader = new HBox(12);
            recommendedHeader.setAlignment(Pos.CENTER_LEFT);
            Label recIcon = new Label("✨");
            recIcon.setStyle(
                    "-fx-font-size: 16px; -fx-text-fill: #3b82f6; -fx-background-color: #dbeafe; -fx-padding: 8 12; -fx-background-radius: 8;");
            VBox recTexts = new VBox(2);
            Label lblR1 = new Label("Picked For You");
            lblR1.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 14px;");
            Label lblR2 = new Label("PERSONALIZED FOR YOU");
            lblR2.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8; -fx-font-size: 10px;");
            recTexts.getChildren().addAll(lblR1, lblR2);
            recommendedHeader.getChildren().addAll(recIcon, recTexts);
            recommendedCol.getChildren().add(recommendedHeader);

            boolean toTrending = true;
            int trendingCount = 0;
            int recommendedCount = 0;

            for (Idea i : filtered) {
                if (trendingCount >= 5 && recommendedCount >= 5)
                    break;

                if (toTrending) {
                    if (trendingCount < 5) {
                        trendingCol.getChildren().add(buildDiscoverCard(i));
                        trendingCount++;
                    } else {
                        recommendedCol.getChildren().add(buildDiscoverCard(i));
                        recommendedCount++;
                    }
                } else {
                    if (recommendedCount < 5) {
                        recommendedCol.getChildren().add(buildDiscoverCard(i));
                        recommendedCount++;
                    } else {
                        trendingCol.getChildren().add(buildDiscoverCard(i));
                        trendingCount++;
                    }
                }
                toTrending = !toTrending;
            }
            discoverLayout.getChildren().addAll(trendingCol, recommendedCol);
            ideasList.getChildren().add(discoverLayout);
        }
    }

    // ── Row Builder (Browse All — matches platform table row) ─────────────────
    private HBox buildTableRow(Idea i) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2); " +
                "-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Idea Info
        VBox ideaInfo = new VBox(4);
        ideaInfo.setPrefWidth(300);
        Label lblTitle = new Label(i.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");
        String desc = i.getDescription() != null
                ? (i.getDescription().length() > 60 ? i.getDescription().substring(0, 60) + "…" : i.getDescription())
                : "";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        ideaInfo.getChildren().addAll(lblTitle, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Category Badge — matches platform gradient style
        Label lblCategory = new Label(i.getCategory() != null ? i.getCategory().toUpperCase() : "GENERAL");
        lblCategory.setStyle("-fx-background-color: #94a3b810; -fx-text-fill: #64748b; " +
                "-fx-padding: 5 12; -fx-background-radius: 10; " +
                "-fx-font-weight: bold; -fx-font-size: 10px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        lblCategory.setMinWidth(160);
        lblCategory.setAlignment(Pos.CENTER);

        // Actions
        HBox actions = new HBox(8);
        actions.setPrefWidth(150);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 0 6 0 0;");

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #ef444415; -fx-text-fill: #ef4444; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            if (gui.util.AlertHelper.showCustomAlert("Delete Idea?", "Are you sure you want to delete this idea?",
                    gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    ideaService.supprimer(i.getId());
                    allIdeas.remove(i);
                    populateCategories();
                    filterAndRender();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        actions.getChildren().add(btnDelete);
        row.getChildren().addAll(ideaInfo, spacer, lblCategory, actions);

        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-padding: 15; -fx-background-color: white;  -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    // ── Card Builder (Discover view — matches platform recommendation card) ────
    private VBox buildDiscoverCard(Idea i) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 15, 0, 0, 8); " +
                "-fx-border-color: #f1f5f9; -fx-border-radius: 18; -fx-cursor: hand;");
        card.setMaxWidth(Double.MAX_VALUE);

        // Category badge (blue gradient inspired by platform)
        Label lblCat = new Label(i.getCategory() != null ? i.getCategory().toUpperCase() : "GENERAL");
        lblCat.setStyle("-fx-background-color: #3b82f620; -fx-text-fill: #3b82f6; " +
                "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 8;");

        Label lblTitle = new Label(i.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        lblTitle.setWrapText(true);

        String desc = i.getDescription() != null
                ? (i.getDescription().length() > 80 ? i.getDescription().substring(0, 80) + "…" : i.getDescription())
                : "";
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        lblDesc.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(lblCat, lblTitle, lblDesc, spacer);

        card.setOnMouseEntered(
                e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(203,12,159,0.12), 20, 0, 0, 5); " +
                        "-fx-border-color: #f0abfc; -fx-border-radius: 18; -fx-cursor: hand;"));
        card.setOnMouseExited(
                e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-padding: 18; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 15, 0, 0, 8); " +
                        "-fx-border-color: #f1f5f9; -fx-border-radius: 18; -fx-cursor: hand;"));
        return card;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
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

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) ideasList.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;
        if (root instanceof BorderPane)
            view = ((BorderPane) root).getCenter();
        if (contentArea != null)
            contentArea.getChildren().setAll(view);
        else {
            javafx.stage.Stage stage = (javafx.stage.Stage) ideasList.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void handleAddIdea() throws Exception {
        StackPane contentArea = (StackPane) ideasList.getScene().lookup("#contentArea");
        if (contentArea != null)
            contentArea.getChildren()
                    .setAll((javafx.scene.Node) FXMLLoader.load(getClass().getResource("/TSK/AddIdea.fxml")));
        else {
            javafx.stage.Stage stage = (javafx.stage.Stage) ideasList.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(FXMLLoader.load(getClass().getResource("/TSK/AddIdea.fxml"))));
        }
    }

    @FXML
    public void goBack() throws Exception {
        goToIdea();
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
}
