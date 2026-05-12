package gui;

import entities.HelpTicket;
import entities.Users;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.HelpTicketService;
import services.UsersService;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SupportController {

    private final HelpTicketService ticketService = new HelpTicketService();
    private final UsersService userService = new UsersService();
    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private List<HelpTicket> allTickets = new ArrayList<>();
    private String currentFilter = "Unread";

    @FXML private Label lblTotalOpen;
    @FXML private Label lblPendingReply;
    @FXML private Label lblUrgentCount;
    @FXML private Label lblResolvedToday;
    @FXML private Label lblAvgResponseTime;
    @FXML private VBox messagesContainer;
    @FXML private TextField searchField;

    @FXML private Button btnMyMessages;
    @FXML private Button btnUnread;
    @FXML private Button btnUrgent;
    @FXML private Button btnToday;
    @FXML private Button btnWeek;

    @FXML
    public void onBack() {
        System.out.println("[SupportController] Back arrow clicked!");
        
        // 1. Try singleton first
        if (HelpDeskMessagesController.getInstance() != null) {
            System.out.println("[SupportController] Found Admin Shell instance. Navigating...");
            Platform.runLater(() -> {
                HelpDeskMessagesController.getInstance().onShowCourses();
            });
            return;
        }

        // 2. Fallback: Search in Scene
        System.out.println("[SupportController] Singleton null, trying Scene lookup...");
        try {
            javafx.scene.Scene scene = searchField.getScene();
            if (scene != null) {
                StackPane contentArea = (StackPane) scene.lookup("#mainContentContainer");
                if (contentArea == null) contentArea = (StackPane) scene.lookup("#contentArea");
                
                if (contentArea != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/admin-courses-view.fxml"));
                    Parent root = loader.load();
                    contentArea.getChildren().setAll(root);
                    System.out.println("[SupportController] Manual navigation successful.");
                } else {
                    System.err.println("[SupportController] Could not find content container in scene.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadData();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSearch());
        }
    }

    private void loadData() {
        try {
            allTickets = ticketService.getAll();
            updateStats();
            applyFiltersAndSearch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        long totalOpen = allTickets.stream().filter(t -> !"RESOLVED".equalsIgnoreCase(t.getStatus())).count();
        long pendingReply = allTickets.stream().filter(t -> t.getAdminResponse() == null || t.getAdminResponse().isEmpty()).count();
        long urgent = allTickets.stream().filter(t -> "Urgent".equalsIgnoreCase(t.getPriority()) || "Haute".equalsIgnoreCase(t.getPriority())).count();
        long resolved = allTickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count();

        lblTotalOpen.setText(String.valueOf(totalOpen));
        lblPendingReply.setText(String.valueOf(pendingReply));
        lblUrgentCount.setText(String.valueOf(urgent));
        lblResolvedToday.setText(String.valueOf(resolved));
        lblAvgResponseTime.setText("1.8h");

        lblUrgentCount.setStyle("-fx-text-fill: #ce2d7c;"); 
        lblPendingReply.setStyle("-fx-text-fill: #434a75;"); 
        lblAvgResponseTime.setStyle("-fx-text-fill: #6c2db1;"); 
    }

    @FXML private void filterMyMessages() { currentFilter = "My"; updateFilterButtonStyles(btnMyMessages); applyFiltersAndSearch(); }
    @FXML private void filterUnread() { currentFilter = "Unread"; updateFilterButtonStyles(btnUnread); applyFiltersAndSearch(); }
    @FXML private void filterUrgent() { currentFilter = "Urgent"; updateFilterButtonStyles(btnUrgent); applyFiltersAndSearch(); }
    @FXML private void filterToday() { currentFilter = "Today"; updateFilterButtonStyles(btnToday); applyFiltersAndSearch(); }
    @FXML private void filterThisWeek() { currentFilter = "Week"; updateFilterButtonStyles(btnWeek); applyFiltersAndSearch(); }

    private void updateFilterButtonStyles(Button activeBtn) {
        List<Button> allBtns = List.of(btnMyMessages, btnUnread, btnUrgent, btnToday, btnWeek);
        for (Button b : allBtns) {
            if (b == null) continue;
            b.getStyleClass().removeAll("btn-primary", "btn-action-light");
            if (b == activeBtn) {
                b.getStyleClass().add("btn-primary");
            } else {
                b.getStyleClass().add("btn-action-light");
            }
        }
    }

    private void applyFiltersAndSearch() {
        String search = searchField.getText().toLowerCase();
        LocalDateTime now = LocalDateTime.now();

        List<HelpTicket> filtered = allTickets.stream()
                .filter(t -> {
                    if (currentFilter == null || "Unread".equals(currentFilter)) {
                         return t.getAdminResponse() == null || t.getAdminResponse().isEmpty();
                    }
                    
                    switch (currentFilter) {
                        case "Urgent": return "Urgent".equalsIgnoreCase(t.getPriority()) || "Haute".equalsIgnoreCase(t.getPriority());
                        case "Today":
                            LocalDateTime createdT = parseDate(t.getCreatedAt());
                            return createdT != null && createdT.toLocalDate().isEqual(now.toLocalDate());
                        case "Week":
                            LocalDateTime createdW = parseDate(t.getCreatedAt());
                            return createdW != null && createdW.isAfter(now.minusDays(7));
                        default: return true;
                    }
                })
                .filter(t -> t.getSubject().toLowerCase().contains(search) || t.getMessage().toLowerCase().contains(search))
                .collect(Collectors.toList());

        renderGroupedTickets(filtered);
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            // Handle common SQL format variations
            String cleaned = dateStr.replace("T", " ");
            if (cleaned.length() > 19) cleaned = cleaned.substring(0, 19);
            return LocalDateTime.parse(cleaned, DB_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private void renderGroupedTickets(List<HelpTicket> tickets) {
        messagesContainer.getChildren().clear();
        if (tickets.isEmpty()) {
            Label empty = new Label("No messages found.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 40;");
            messagesContainer.getChildren().add(empty);
            return;
        }

        List<HelpTicket> urgent = tickets.stream().filter(t -> ("Urgent".equalsIgnoreCase(t.getPriority()) || "Haute".equalsIgnoreCase(t.getPriority())) && !"RESOLVED".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList());
        List<HelpTicket> pending = tickets.stream().filter(t -> !urgent.contains(t) && !"RESOLVED".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList());
        List<HelpTicket> resolved = tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList());

        if (!urgent.isEmpty()) {
            addSectionHeader("🔴 Urgent", "#ce2d7c");
            urgent.forEach(t -> messagesContainer.getChildren().add(createMessageRow(t)));
        }

        if (!pending.isEmpty()) {
            addSectionHeader("🟡 Pending", "#434a75");
            pending.forEach(t -> messagesContainer.getChildren().add(createMessageRow(t)));
        }

        if (!resolved.isEmpty()) {
            TitledPane resolvedSection = new TitledPane();
            resolvedSection.setText("🟢 Resolved (" + resolved.size() + ")");
            resolvedSection.setExpanded(false);
            resolvedSection.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");

            VBox resolvedBox = new VBox(12);
            resolvedBox.setPadding(new Insets(15, 0, 0, 0));
            resolved.forEach(t -> resolvedBox.getChildren().add(createMessageRow(t)));
            resolvedSection.setContent(resolvedBox);
            messagesContainer.getChildren().add(resolvedSection);
        }
    }

    private void addSectionHeader(String title, String color) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-padding: 10 0 5 0;");
        messagesContainer.getChildren().add(label);
    }

    private Node createMessageRow(HelpTicket t) {
        VBox cardContainer = new VBox(0);
        cardContainer.getStyleClass().add("card");
        cardContainer.setPadding(Insets.EMPTY);
        cardContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-overflow: hidden; -fx-cursor: hand;");

        HBox mainRow = new HBox(20);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(15, 20, 15, 20));

        HBox creatorBox = new HBox(15);
        creatorBox.setAlignment(Pos.CENTER_LEFT);
        creatorBox.setPrefWidth(320);

        StackPane avatar = new StackPane();
        Circle circle = new Circle(18, Color.web("#f1f5f9"));
        Label initials = new Label("?");
        try {
            Users user = userService.getUserById(t.getCreatorId());
            if (user != null && user.getUsername() != null) {
                initials.setText(user.getUsername().substring(0, 1).toUpperCase());
            }
        } catch (Exception e) {}
        initials.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
        avatar.getChildren().addAll(circle, initials);

        VBox textBox = new VBox(2);
        Label name = new Label("👤 Creator #" + t.getCreatorId());
        try {
            Users u = userService.getUserById(t.getCreatorId());
            if(u != null) name.setText("👤 " + u.getUsername());
        } catch(Exception e) {}
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
        Label snippet = new Label("📝 " + t.getSubject());
        snippet.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        textBox.getChildren().addAll(name, snippet);
        creatorBox.getChildren().addAll(avatar, textBox);

        StackPane priorityContainer = new StackPane();
        priorityContainer.setPrefWidth(100);
        String pText = t.getPriority();
        String pEmoji = "⚪ ";
        if ("Urgent".equalsIgnoreCase(pText) || "Haute".equalsIgnoreCase(pText)) pEmoji = "🔴 ";
        else if ("Moyenne".equalsIgnoreCase(pText)) pEmoji = "🟡 ";

        Label priorityLabel = new Label(pEmoji + pText);
        priorityLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 4 10;");
        priorityContainer.getChildren().add(priorityLabel);

        StackPane statusContainer = new StackPane();
        statusContainer.setPrefWidth(100);
        boolean isUnread = t.getAdminResponse() == null || t.getAdminResponse().isEmpty();
        Label statusLabel = new Label(isUnread ? "📥 Unread" : "📤 Replied");
        statusLabel.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 4 10;");
        statusContainer.getChildren().add(statusLabel);

        Label timeAgoLabel = new Label("🕒 " + getTimeAgo(t.getCreatedAt()));
        timeAgoLabel.setPrefWidth(110);
        timeAgoLabel.setAlignment(Pos.CENTER);
        timeAgoLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button replyBtn = new Button("Reply");
        replyBtn.getStyleClass().add("btn-primary");
        replyBtn.setPrefWidth(90);
        replyBtn.setPrefHeight(35);

        mainRow.getChildren().addAll(creatorBox, priorityContainer, statusContainer, timeAgoLabel, spacer, replyBtn);

        VBox replyArea = new VBox(15);
        replyArea.setPadding(new Insets(10, 25, 25, 25));
        replyArea.setVisible(false);
        replyArea.setManaged(false);

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 0, 10, 0));

        TextArea responseField = new TextArea(t.getAdminResponse());
        responseField.setPromptText("Type your response to the creator...");
        responseField.setPrefHeight(100);
        responseField.setWrapText(true);
        responseField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f0; -fx-padding: 10; -fx-background-color: #fafafa;");

        Label successBanner = new Label("✅ Response sent successfully!");
        successBanner.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-padding: 8 15; -fx-background-radius: 8; -fx-font-weight: bold;");
        successBanner.setVisible(false);
        successBanner.setManaged(false);

        HBox replyActions = new HBox(12);
        replyActions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-action-light");
        cancelBtn.setMinWidth(100);

        Button sendBtn = new Button("Send Response ✈️");
        sendBtn.getStyleClass().add("btn-primary");

        Runnable toggle = () -> {
            boolean isExpanded = replyArea.isVisible();
            replyArea.setVisible(!isExpanded);
            replyArea.setManaged(!isExpanded);
            cardContainer.setStyle(isExpanded ? "-fx-background-color: white; -fx-background-radius: 15;" : "-fx-background-color: #fcfcfd; -fx-background-radius: 15; -fx-border-color: -fx-primary-pink; -fx-border-radius: 15; -fx-border-width: 1.5;");
        };

        cancelBtn.setOnAction(e -> toggle.run());
        sendBtn.setOnAction(e -> {
            String msg = responseField.getText();
            if (msg == null || msg.trim().isEmpty()) {
                responseField.setStyle(responseField.getStyle() + "-fx-border-color: #dc2626; -fx-border-width: 2;");
                return;
            }
            responseField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e2e8f0; -fx-padding: 10; -fx-background-color: #fafafa;");

            t.setAdminResponse(msg);
            t.setStatus("IN PROGRESS");
            saveTicket(t);

            statusLabel.setText("📤 Replied");
            successBanner.setVisible(true);
            successBanner.setManaged(true);

            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ex) {}
                Platform.runLater(() -> {
                    successBanner.setVisible(false);
                    successBanner.setManaged(false);
                    toggle.run();
                });
            }).start();
        });

        replyActions.getChildren().addAll(cancelBtn, sendBtn);
        replyArea.getChildren().addAll(sep, responseField, successBanner, replyActions);

        mainRow.setOnMouseClicked(e -> toggle.run());
        replyBtn.setOnAction(e -> toggle.run());

        cardContainer.getChildren().addAll(mainRow, replyArea);
        return cardContainer;
    }

    private String getTimeAgo(String dateTimeStr) {
        if (dateTimeStr == null) return "Recently";
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTimeStr, DB_FORMAT);
            Duration d = Duration.between(dt, LocalDateTime.now());
            if (d.toMinutes() < 1) return "Just now";
            if (d.toMinutes() < 60) return d.toMinutes() + " mins ago";
            if (d.toHours() < 24) return d.toHours() + " hours ago";
            return d.toDays() + " days ago";
        } catch (Exception e) { return dateTimeStr.split(" ")[0]; }
    }

    private void saveTicket(HelpTicket t) {
        try {
            ticketService.update(t);
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
