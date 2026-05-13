package gui.TSKControllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entities.Mission;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import services.MissionService;
import utils.SessionManager;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import org.json.JSONArray;
import org.json.JSONObject;
import netscape.javascript.JSObject;

public class MissionController {
    @FXML
    private Button btnAdmin;
    @FXML
    private VBox missionsList;
    @FXML
    private Label lblNavUsername;
    @FXML
    private Label lblNavUserRole;

    // New UI refs
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnViewCalendar;
    @FXML
    private Button btnViewList;
    @FXML
    private HBox tableControls;
    @FXML
    private VBox calendarPane;
    @FXML
    private HBox columnHeaders;
    @FXML
    private Button btnStatusNew;
    @FXML
    private Button btnStatusInProgress;
    @FXML
    private Button btnStatusCompleted;

    private final MissionService missionService = new MissionService();
    private final services.IdeaService ideaService = new services.IdeaService();
    private List<Mission> allMissions = new ArrayList<>();
    private final Set<String> activeStatuses = new HashSet<>();
    private boolean listMode = false; // default: calendar view

    // ── Styles ────────────────────────────────────────────────────────────────
    private static final String BTN_SWITCH_ACTIVE = "-fx-background-color: white; -fx-text-fill: #cb0c9f; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.07),6,0,0,2); -fx-cursor: hand;";
    private static final String BTN_SWITCH_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-padding: 7 18; -fx-cursor: hand;";
    private static final String STATUS_BTN_BASE = "-fx-background-color: #f8fafc; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: #e2e8f0; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        gui.FrontMainController.setNavbarText("Missions Overview", "Pages / Workflow / Missions");
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        if (btnAdmin != null) {
            btnAdmin.setVisible(isAdmin);
            btnAdmin.setManaged(isAdmin);
        }

        entities.Users current = SessionManager.getInstance().getCurrentUser();
        if (current != null && lblNavUsername != null) {
            lblNavUsername.setText(current.getUsername());
            String role = current.getRole() != null ? current.getRole().replace("ROLE_", "") : "USER";
            lblNavUserRole.setText(role);
        }

        loadMissions();
        applyView(false); // Start on calendar view
    }

    // ── View Switching ─────────────────────────────────────────────────────────
    @FXML
    public void switchToCalendar() {
        applyView(false);
    }

    @FXML
    public void switchToList() {
        applyView(true);
    }

    private void applyView(boolean list) {
        listMode = list;
        if (calendarPane != null) {
            calendarPane.setVisible(!list);
            calendarPane.setManaged(!list);
        }
        if (missionsList != null) {
            missionsList.setVisible(list);
            missionsList.setManaged(list);
        }
        if (tableControls != null) {
            tableControls.setVisible(list);
            tableControls.setManaged(list);
        }
        if (columnHeaders != null) {
            columnHeaders.setVisible(list);
            columnHeaders.setManaged(list);
        }

        if (btnViewCalendar != null)
            btnViewCalendar.setStyle(list ? BTN_SWITCH_INACTIVE : BTN_SWITCH_ACTIVE);
        if (btnViewList != null)
            btnViewList.setStyle(list ? BTN_SWITCH_ACTIVE : BTN_SWITCH_INACTIVE);

        if (list)
            filterAndRender();
        else
            renderCalendar();
    }

    // ── Status Filter Toggles ─────────────────────────────────────────────────
    @FXML
    public void toggleStatusNew() {
        toggleStatus("new", btnStatusNew);
    }

    @FXML
    public void toggleStatusInProgress() {
        toggleStatus("in_progress", btnStatusInProgress);
    }

    @FXML
    public void toggleStatusCompleted() {
        toggleStatus("completed", btnStatusCompleted);
    }

    private void toggleStatus(String status, Button btn) {
        if (activeStatuses.contains(status)) {
            activeStatuses.remove(status);
            if (btn != null)
                btn.setStyle(STATUS_BTN_BASE);
        } else {
            activeStatuses.add(status);
            String activeStyle = STATUS_BTN_BASE;
            if ("new".equals(status))
                activeStyle = "-fx-background-color: linear-gradient(to right,#7928CA,#cb0c9f); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            else if ("in_progress".equals(status))
                activeStyle = "-fx-background-color: linear-gradient(to right,#3b82f6,#06b6d4); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            else if ("completed".equals(status))
                activeStyle = "-fx-background-color: linear-gradient(to right,#22c55e,#84cc16); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 9; -fx-border-color: transparent; -fx-border-radius: 9; -fx-padding: 7 16; -fx-cursor: hand;";
            if (btn != null)
                btn.setStyle(activeStyle);
        }
        filterAndRender();
    }

    // ── Search ────────────────────────────────────────────────────────────────
    @FXML
    public void onSearch() {
        filterAndRender();
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadMissions() {
        try {
            entities.Users user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                allMissions = missionService.afficher(user.getId(), user.getRole());
            } else {
                allMissions = missionService.afficher(); // Fallback
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        filterAndRender();
    }

    private void filterAndRender() {
        if (missionsList == null)
            return;
        missionsList.getChildren().clear();

        String search = (txtSearch != null) ? txtSearch.getText().toLowerCase().trim() : "";

        for (Mission m : allMissions) {
            String title = m.getTitle() != null ? m.getTitle().toLowerCase() : "";
            String desc = m.getDescription() != null ? m.getDescription().toLowerCase() : "";
            String state = m.getState() != null ? m.getState().toLowerCase() : "";

            entities.Idea associatedIdea = (m.getImplement_idea_id() > 0)
                    ? ideaService.getIdeaById(m.getImplement_idea_id())
                    : null;
            String idea = associatedIdea != null ? associatedIdea.getTitle().toLowerCase() : "";

            boolean matchesSearch = search.isEmpty() || title.contains(search) || desc.contains(search)
                    || idea.contains(search);
            boolean matchesStatus = activeStatuses.isEmpty() || activeStatuses.contains(state);

            if (matchesSearch && matchesStatus) {
                missionsList.getChildren().add(buildMissionRow(m));
            }
        }

        if (missionsList.getChildren().isEmpty()) {
            Label empty = new Label("No missions found.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 20;");
            missionsList.getChildren().add(empty);
        }
    }

    /*
     * OLD CALENDARFX CODE
     * private void renderCalendarOld() {
     * if (calendarPane == null)
     * return;
     * calendarPane.getChildren().clear();
     * calendarPane.setAlignment(Pos.TOP_LEFT);
     * 
     * // Header and switch button
     * HBox headerBox = new HBox();
     * headerBox.setAlignment(Pos.CENTER_LEFT);
     * headerBox.setStyle("-fx-padding: 0 0 20 0;");
     * 
     * VBox titleBox = new VBox(4);
     * Label lblHeader = new Label("📅 Mission Calendar");
     * lblHeader.
     * setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;"
     * );
     * Label lblSub = new Label("Missions organized using CalendarFX");
     * lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
     * titleBox.getChildren().addAll(lblHeader, lblSub);
     * 
     * Region spacer = new Region();
     * HBox.setHgrow(spacer, Priority.ALWAYS);
     * 
     * Button btnSwitch = new Button("Browse All Missions");
     * btnSwitch.setStyle(
     * "-fx-background-color: linear-gradient(to right, #cb0c9f, #6c2db1); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 12; -fx-padding: 10 24; -fx-cursor: hand;"
     * );
     * btnSwitch.setOnAction(e -> switchToList());
     * 
     * headerBox.getChildren().addAll(titleBox, spacer, btnSwitch);
     * calendarPane.getChildren().add(headerBox);
     * 
     * try {
     * com.calendarfx.view.CalendarView calendarView = new
     * com.calendarfx.view.CalendarView();
     * 
     * com.calendarfx.model.Calendar missionCalendar = new
     * com.calendarfx.model.Calendar("Missions");
     * missionCalendar.setStyle(com.calendarfx.model.Calendar.Style.STYLE1);
     * 
     * com.calendarfx.model.CalendarSource myCalendarSource = new
     * com.calendarfx.model.CalendarSource(
     * "My Missions");
     * myCalendarSource.getCalendars().addAll(missionCalendar);
     * 
     * calendarView.getCalendarSources().addAll(myCalendarSource);
     * calendarView.setRequestedTime(java.time.LocalTime.now());
     * 
     * // Add missions to calendar
     * for (Mission m : allMissions) {
     * String dateStr = m.getMission_date();
     * if (dateStr != null && !dateStr.trim().isEmpty()) {
     * try {
     * String cleanDate = dateStr.trim();
     * if (cleanDate.contains(" ")) {
     * cleanDate = cleanDate.split(" ")[0]; // remove time part if exists e.g.
     * '2024-05-10
     * // 14:00:00'
     * }
     * 
     * java.time.LocalDate date;
     * try {
     * date = java.time.LocalDate.parse(cleanDate);
     * } catch (Exception e1) {
     * try {
     * date = java.time.LocalDate.parse(cleanDate,
     * java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
     * } catch (Exception e2) {
     * date = java.time.LocalDate.parse(cleanDate,
     * java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
     * }
     * }
     * 
     * com.calendarfx.model.Entry<Mission> entry = new
     * com.calendarfx.model.Entry<>(m.getTitle());
     * entry.setUserObject(m); // CRITICAL: Link the actual Mission object to the
     * calendar entry
     * entry.changeStartDate(date);
     * entry.changeEndDate(date);
     * entry.setFullDay(true);
     * missionCalendar.addEntry(entry);
     * } catch (Exception ex) {
     * System.err.println("Failed to map mission to Calendar due to date format: " +
     * dateStr);
     * }
     * }
     * }
     * 
     * // Customize CalendarView UI
     * calendarView.setShowAddCalendarButton(false);
     * calendarView.setShowPrintButton(false);
     * calendarView.setShowDeveloperConsole(false);
     * calendarView.setShowSearchField(false);
     * calendarView.setShowSourceTrayButton(false);
     * calendarView.showMonthPage(); // Default to Month View
     * 
     * // Prevent default popover entirely
     * calendarView.setEntryDetailsCallback(param -> true);
     * 
     * // Completely override the Context Menu to provide exactly the
     * \"Information\" option
     * calendarView.setEntryContextMenuCallback(param -> {
     * javafx.scene.control.ContextMenu contextMenu = new
     * javafx.scene.control.ContextMenu();
     * com.calendarfx.model.Entry<?> entry = param.getEntry();
     * 
     * // Now that UserObject is correctly set, this check will pass
     * if (entry != null && entry.getUserObject() instanceof Mission) {
     * Mission m = (Mission) entry.getUserObject();
     * javafx.scene.control.MenuItem infoItem = new
     * javafx.scene.control.MenuItem("Information");
     * infoItem.setStyle("-fx-font-size: 13px; -fx-padding: 5 15;");
     * infoItem.setOnAction(e -> javafx.application.Platform.runLater(() ->
     * showMissionDetailsDialog(m)));
     * contextMenu.getItems().add(infoItem);
     * }
     * return contextMenu;
     * });
     * 
     * VBox.setVgrow(calendarView, Priority.ALWAYS);
     * calendarPane.getChildren().add(calendarView);
     * } catch (Exception e) {
     * e.printStackTrace();
     * Label err = new
     * Label("Failed to load CalendarFX view. Please restart or rebuild.");
     * err.setStyle("-fx-text-fill: red;");
     * calendarPane.getChildren().add(err);
     * }
     * }
     */

    // ── Calendar WebView Integration ──────────────────────────────────────────
    private void renderCalendar() {
        if (calendarPane == null)
            return;
        calendarPane.getChildren().clear();

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Prepare JSON data for the calendar
        JSONArray calendarData = new JSONArray();
        for (Mission m : allMissions) {
            String state = m.getState() != null ? m.getState().toLowerCase() : "new";
            String color = "completed".equals(state) ? "#10b981"
                    : ("in_progress".equals(state) ? "#3b82f6" : "#cb0c9f");

            JSONObject entry = new JSONObject();
            entry.put("id", String.valueOf(m.getId()));
            entry.put("title", m.getTitle());
            entry.put("start", m.getMission_datetime());
            entry.put("backgroundColor", color);
            entry.put("borderColor", color);
            entry.put("description", m.getDescription() != null ? m.getDescription() : "No description");
            entry.put("state", state);

            entities.Idea assocIdea = (m.getImplement_idea_id() > 0) ? ideaService.getIdeaById(m.getImplement_idea_id())
                    : null;
            entry.put("idea", assocIdea != null ? assocIdea.getTitle() : "N/A");
            entry.put("creator", m.getCreatorName() != null ? m.getCreatorName() : "Unknown"); 

            calendarData.put(entry);
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.10/index.global.min.js'></script>\n" +
                "    <script src=\"https://unpkg.com/@popperjs/core@2\"></script>\n" +
                "    <script src=\"https://unpkg.com/tippy.js@6\"></script>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/tippy.js@6/dist/tippy.css\"/>\n" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css\" rel=\"stylesheet\">\n"
                +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n"
                +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; font-family: 'Inter', sans-serif; overflow: hidden; background: white; }\n"
                +
                "        #calendar { height: 100vh; padding: 15px; }\n" +
                "        .fc .fc-button-primary { background-color: #cb0c9f; border-color: #cb0c9f; }\n" +
                "        .fc .fc-button-primary:hover { background-color: #83107a; border-color: #83107a; }\n" +
                "        .fc-event { cursor: pointer; transition: transform 0.2s ease; border-radius: 6px !important; padding: 2px 4px; }\n"
                +
                "        .fc-event:hover { transform: scale(1.02); z-index: 999; }\n" +
                "        .fc-daygrid-day-frame { cursor: pointer; }\n" +
                "        ::-webkit-scrollbar { display: none; }\n" +
                "        .tippy-box[data-theme~='creaco'] { background-color: white; color: #344767; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border-radius: 1rem; padding: 0.5rem; border: 1px solid #e2e8f0; width: 250px; }\n"
                +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"calendar\"></div>\n" +
                "    <script>\n" +
                "        document.addEventListener('DOMContentLoaded', function() {\n" +
                "            var calendarEl = document.getElementById('calendar');\n" +
                "            var calendar = new FullCalendar.Calendar(calendarEl, {\n" +
                "                initialView: 'dayGridMonth',\n" +
                "                headerToolbar: { left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek,listMonth' },\n"
                +
                "                events: " + calendarData.toString() + ",\n" +
                "                dateClick: function(info) {\n" +
                "                    if (window.javaConnector) {\n" +
                "                        window.javaConnector.onDateClick(info.dateStr);\n" +
                "                    }\n" +
                "                },\n" +
                "                eventClick: function(info) {\n" +
                "                    if (window.javaConnector) {\n" +
                "                        window.javaConnector.onEventClick(info.event.id);\n" +
                "                    }\n" +
                "                },\n" +
                "                eventDidMount: function(info) {\n" +
                "                    const desc = info.event.extendedProps.description;\n" +
                "                    const state = info.event.extendedProps.state;\n" +
                "                    const idea = info.event.extendedProps.idea;\n" +
                "                    const creator = info.event.extendedProps.creator;\n" +
                "                    \n" +
                "                    let stateBadgeClass = 'bg-gradient-to-tl from-slate-600 to-slate-300';\n" +
                "                    if (state === 'new') stateBadgeClass = 'bg-gradient-to-tl from-purple-700 to-pink-500';\n"
                +
                "                    else if (state === 'in_progress') stateBadgeClass = 'bg-gradient-to-tl from-blue-600 to-cyan-400';\n"
                +
                "                    else if (state === 'completed') stateBadgeClass = 'bg-gradient-to-tl from-green-600 to-lime-400';\n"
                +
                "\n" +
                "                    tippy(info.el, {\n" +
                "                        content: `\n" +
                "                            <div class=\"p-3 w-full text-left\">\n" +
                "                                <div class=\"mb-2\">\n" +
                "                                    <h6 class=\"text-xs font-bold m-0 text-slate-700\">${info.event.title}</h6>\n"
                +
                "                                    <span class=\"${stateBadgeClass} px-2 py-0.5 text-[9px] rounded-1.8 font-bold text-white uppercase block w-fit mt-1\">${state}</span>\n"
                +
                "                                    <span class=\"text-[10px] font-medium text-slate-500 block mt-1\"><i class=\"fas fa-user text-slate-400 mr-1\"></i> ${creator}</span>\n"
                +
                "                                </div>\n" +
                "                                <div class=\"mb-2\">\n" +
                "                                    <p class=\"text-[10px] text-slate-600 line-clamp-4 m-0\">${desc}</p>\n"
                +
                "                                </div>\n" +
                "                                <div class=\"flex items-center pt-2 border-t border-gray-100\">\n" +
                "                                    <span class=\"text-[9px] font-bold text-slate-700\">Idea: ${idea}</span>\n"
                +
                "                                </div>\n" +
                "                            </div>`,\n" +
                "                        allowHTML: true, theme: 'creaco', interactive: true, placement: 'top', animation: 'shift-away'\n"
                +
                "                    });\n" +
                "                }\n" +
                "            });\n" +
                "            calendar.render();\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
            }
        });

        engine.loadContent(html);
        webView.setPrefHeight(700); // Fixed height to ensure it's clickable and visible in ScrollPane
        VBox.setVgrow(webView, Priority.ALWAYS);
        calendarPane.getChildren().add(webView);
    }

    public class JavaConnector {
        public void onEventClick(String id) {
            try {
                int missionId = Integer.parseInt(id);
                Mission m = missionService.getMissionById(missionId);
                if (m != null) {
                    javafx.application.Platform.runLater(() -> openEditMission(m));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onDateClick(String dateStr) {
            try {
                java.time.LocalDate clickedDate = java.time.LocalDate.parse(dateStr);
                java.time.LocalDate today = java.time.LocalDate.now();

                if (clickedDate.isBefore(today)) {
                    javafx.application.Platform.runLater(() -> {
                        gui.util.AlertHelper.showCustomAlert(
                            "Invalid Date", 
                            "Missions cannot be scheduled in the past.", 
                            gui.util.AlertHelper.AlertType.WARNING
                        );
                    });
                    return;
                }

                javafx.application.Platform.runLater(() -> {
                    try {
                        handleAddMission(clickedDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ── Row Builder ───────────────────────────────────────────────────────────
    private HBox buildMissionRow(Mission m) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 2); " +
                "-fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;");

        // Mission Info
        VBox missionInfo = new VBox(4);
        missionInfo.setPrefWidth(300);
        Label lblTitle = new Label(m.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        entities.Idea associatedIdea = (m.getImplement_idea_id() > 0)
                ? ideaService.getIdeaById(m.getImplement_idea_id())
                : null;
        String ideaStr = (associatedIdea != null) ? "Idea: " + associatedIdea.getTitle() : "No Idea associated";

        Tooltip ideaTooltip = new Tooltip(ideaStr);
        ideaTooltip.setStyle(
                "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8px; -fx-background-radius: 6px;");
        Tooltip.install(lblTitle, ideaTooltip);

        String desc = m.getDescription() != null
                ? (m.getDescription().length() > 60 ? m.getDescription().substring(0, 60) + "…" : m.getDescription())
                : ideaStr;
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        missionInfo.getChildren().addAll(lblTitle, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Idea Column
        String ideaTitle = (associatedIdea != null && associatedIdea.getTitle() != null) ? associatedIdea.getTitle()
                : "No Idea";
        if (ideaTitle.length() > 20)
            ideaTitle = ideaTitle.substring(0, 20) + "…";
        Label lblIdeaText = new Label(ideaTitle);
        lblIdeaText.setStyle(
                "-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6;");

        HBox lblIdea = new HBox(lblIdeaText);
        lblIdea.setPrefWidth(160);
        lblIdea.setAlignment(Pos.CENTER);

        // Status Badge — matches platform color scheme
        String state = m.getState() != null ? m.getState().toLowerCase() : "new";
        String badgeColor;
        if ("new".equals(state))
            badgeColor = "linear-gradient(to right,#7928CA,#cb0c9f)";
        else if ("in_progress".equals(state))
            badgeColor = "linear-gradient(to right,#3b82f6,#06b6d4)";
        else if ("completed".equals(state))
            badgeColor = "linear-gradient(to right,#22c55e,#84cc16)";
        else
            badgeColor = "linear-gradient(to right,#64748b,#94a3b8)";

        Label lblStatus = new Label(state.replace("_", " ").toUpperCase());
        lblStatus.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; " +
                "-fx-padding: 5 12; -fx-background-radius: 10; " +
                "-fx-font-weight: bold; -fx-font-size: 10px;");
        lblStatus.setPrefWidth(120);
        lblStatus.setAlignment(Pos.CENTER);

        // Date
        Label lblDate = new Label(m.getMission_datetime() != null ? m.getMission_datetime() : "N/A");
        lblDate.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        lblDate.setPrefWidth(120);
        lblDate.setAlignment(Pos.CENTER);

        // Actions
        HBox actions = new HBox(6);
        actions.setPrefWidth(300);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁 View");
        btnView.setStyle(
                "-fx-background-color: #8b5cf615; -fx-text-fill: #8b5cf6; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnView.setOnAction(e -> showMissionDetailsDialog(m));

        Button btnEdit = new Button("✎ Edit");
        btnEdit.setStyle(
                "-fx-background-color: #3b82f615; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnEdit.setOnAction(e -> openEditMission(m));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle(
                "-fx-background-color: #ef444415; -fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> {
            if (gui.util.AlertHelper.showCustomAlert("Delete Mission?", "Are you sure you want to delete this mission?",
                    gui.util.AlertHelper.AlertType.CONFIRMATION)) {
                try {
                    missionService.supprimer(m.getId());
                    allMissions.remove(m);
                    filterAndRender();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button btnAddTask = new Button("➕ Task");
        btnAddTask.setStyle(
                "-fx-background-color: #10b98115; -fx-text-fill: #10b981; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnAddTask.setOnAction(e -> handleAddTaskForMission(m));

        actions.getChildren().addAll(btnAddTask, btnView, btnEdit, btnDelete);
        row.getChildren().addAll(missionInfo, spacer, lblIdea, lblStatus, lblDate, actions);

        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;"));
        row.setOnMouseExited(e -> row.setStyle(
                "-fx-padding: 15; -fx-background-color: white;  -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-width: 0 0 1 0;"));

        return row;
    }

    // ── Dialogs & Helpers ─────────────────────────────────────────────────────
    private void openEditMission(Mission m) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TSK/EditMission.fxml"));
            javafx.scene.Node root = loader.load();
            EditMissionController ctrl = loader.getController();
            ctrl.setMission(m);
            StackPane contentArea = (StackPane) (missionsList != null ? missionsList.getScene().lookup("#contentArea")
                    : calendarPane.getScene().lookup("#contentArea"));
            if (contentArea != null)
                contentArea.getChildren().setAll(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showMissionDetailsDialog(Mission m) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setStyle(
                "-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #cbd5e1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 0);");
        root.setPrefWidth(400);

        Label lblHeader = new Label("Mission Details");
        lblHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String state = m.getState() != null ? m.getState().toLowerCase() : "new";
        entities.Idea associatedIdea = (m.getImplement_idea_id() > 0)
                ? ideaService.getIdeaById(m.getImplement_idea_id())
                : null;

        String details = "Title: " + m.getTitle() + "\n\n" +
                "Description: " + (m.getDescription() != null ? m.getDescription() : "N/A") + "\n\n" +
                "Status: " + state.replace("_", " ").toUpperCase() + "\n\n" +
                "Date: " + (m.getMission_datetime() != null ? m.getMission_datetime() : "N/A");

        if (associatedIdea != null) {
            details += "\n\nImplemented Idea: " + associatedIdea.getTitle() + "\n" +
                    "Idea Category: "
                    + (associatedIdea.getCategory() != null ? associatedIdea.getCategory() : "General") + "\n" +
                    "Idea Description: "
                    + (associatedIdea.getDescription() != null ? associatedIdea.getDescription() : "N/A");
        }

        Label lblDetails = new Label(details);
        lblDetails.setWrapText(true);
        lblDetails.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-line-spacing: 5px;");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button btnEdit = new Button("✎ Edit Mission");
        btnEdit.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            stage.close();
            openEditMission(m);
        });

        Button btnClose = new Button("Close");
        btnClose.setStyle(
                "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        btnClose.setOnAction(e -> stage.close());

        buttons.getChildren().addAll(btnEdit, btnClose);
        root.getChildren().addAll(lblHeader, lblDetails, buttons);

        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    public void goToAdmin() throws Exception {
        switchScene("/Users/Admin.fxml");
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

    private void switchScene(String fxml) throws Exception {
        StackPane contentArea = (StackPane) missionsList.getScene().lookup("#contentArea");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Node view = root;
        if (root instanceof BorderPane)
            view = ((BorderPane) root).getCenter();
        if (contentArea != null)
            contentArea.getChildren().setAll(view);
        else {
            javafx.stage.Stage stage = (javafx.stage.Stage) missionsList.getScene().getWindow();
            stage.getScene().setRoot(root);
        }
    }

    @FXML
    public void handleAddMission() throws Exception {
        handleAddMission(null);
    }

    public void handleAddMission(java.time.LocalDate initialDate) throws Exception {
        StackPane contentArea = (StackPane) (missionsList != null ? missionsList.getScene().lookup("#contentArea") : calendarPane.getScene().lookup("#contentArea"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TSK/AddMission.fxml"));
        javafx.scene.Node root = loader.load();
        
        AddMissionController ctrl = loader.getController();
        if (initialDate != null) {
            ctrl.setInitialDate(initialDate);
        }

        if (contentArea != null) {
            contentArea.getChildren().setAll(root);
        } else {
            javafx.stage.Stage stage = (javafx.stage.Stage) (missionsList != null ? missionsList.getScene().getWindow() : calendarPane.getScene().getWindow());
            stage.setScene(new javafx.scene.Scene((javafx.scene.Parent)root));
        }
    }

    private void handleAddTaskForMission(Mission m) {
        try {
            StackPane contentArea = (StackPane) (missionsList != null ? missionsList.getScene().lookup("#contentArea") : calendarPane.getScene().lookup("#contentArea"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TSK/AddTask.fxml"));
            javafx.scene.Node root = loader.load();
            
            AddTaskController ctrl = loader.getController();
            ctrl.setMission(m);

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() throws Exception {
        goToMission();
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
