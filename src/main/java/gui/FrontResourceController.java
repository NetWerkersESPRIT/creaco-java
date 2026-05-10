package gui;

import entities.Course;
import entities.Question;
import entities.Quiz;
import entities.QuizResult;
import entities.Ressource;
import entities.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuestionService;
import services.QuizResultService;
import services.QuizService;
import services.RessourceService;
import utils.GroqConfig;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import animatefx.animation.*;

public class FrontResourceController {

    private final RessourceService ressourceService = new RessourceService();
    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final QuizResultService quizResultService = new QuizResultService();
    private final services.UserCourseProgressService progressService = new services.UserCourseProgressService();
    private Course currentCourse;
    private List<Ressource> ressources = Collections.emptyList();
    private Quiz activeQuiz;
    private Ressource activeResource;

    @FXML private Label courseTitleLabel;
    @FXML private TilePane resourcesContainer;

    // Profile Navbar labels
    @FXML private Label lblNavUsername;
    @FXML private Label lblNavUserRole;
    @FXML private javafx.scene.layout.HBox profileBox;
    @FXML private Button logoutBtn;

    // Smart Tutor UI
    @FXML private StackPane tutorModal;
    @FXML private VBox chatBox;
    @FXML private TextField chatInput;
    @FXML private ScrollPane chatScroll;
    @FXML private Button askTutorBtn;
    @FXML private VBox mainContent;

    // Resource Modal UI
    @FXML private StackPane resourceModal;
    @FXML private Label modalResourceTitle;
    @FXML private Label modalResourceDesc;

    // Quiz Modal UI
    @FXML private StackPane quizModal;
    @FXML private Label quizModalTitle;
    @FXML private VBox quizQuestionsContainer;
    @FXML private HBox quizResultCard;
    @FXML private Label quizResultIcon;
    @FXML private Label quizResultLabel;
    @FXML private Button submitQuizButton;

    @FXML
    public void initialize() {
        // Entrance Animation
        if (mainContent != null) {
            new FadeIn(mainContent).setSpeed(0.8).play();
        }

        // Button Hover Animation
        if (askTutorBtn != null) {
            askTutorBtn.setOnMouseEntered(e -> new Pulse(askTutorBtn).setSpeed(2.0).play());
        }

        // Initialize User Profile in Navbar
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getUsername() != null ? user.getUsername() : "User";
            if (lblNavUsername != null) lblNavUsername.setText(displayName);
            
            String role = user.getRole() != null ? user.getRole().replace("ROLE_", "") : "USER";
            if (lblNavUserRole != null) {
                lblNavUserRole.setText(role);
                if ("ADMIN".equals(role)) {
                    lblNavUserRole.setStyle("-fx-background-color: #434a75;");
                }
            }
        }
    }

    public void setCourse(Course course) {
        this.currentCourse = course;
        if (courseTitleLabel != null) {
            courseTitleLabel.setText("Resources for: " + course.getTitre());
        }
        loadResources();
    }

    private void loadResources() {
        if (currentCourse == null) return;
        try {
            ressources = ressourceService.afficherParCours(currentCourse.getId());
            renderResources();
        } catch (SQLException e) {
            ressources = Collections.emptyList();
            resourcesContainer.getChildren().clear();
            Label error = new Label("Error loading resources: " + e.getMessage());
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            resourcesContainer.getChildren().add(error);
        }
    }

    private void renderResources() {
        resourcesContainer.getChildren().clear();
        if (ressources.isEmpty()) {
            Label empty = new Label("No resources available for this course.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 16px;");
            resourcesContainer.getChildren().add(empty);
            return;
        }

        for (Ressource ressource : ressources) {
            Node card = buildResourceCard(ressource);
            resourcesContainer.getChildren().add(card);
        }
    }

    private Node buildResourceCard(Ressource ressource) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMinWidth(300);

        Label name = new Label(ressource.getNom());
        name.getStyleClass().add("card-title");
        
        // Check completion status
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            try {
                if (progressService.isResourceCompleted(user.getId(), ressource.getId())) {
                    name.setText("✓ " + ressource.getNom());
                    name.setStyle("-fx-text-fill: #10b981;"); // Green for completed
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        Label type = new Label("Type: " + (ressource.getType() == null ? "-" : ressource.getType()));
        type.getStyleClass().add("badge-pink"); 
        type.setMaxWidth(Region.USE_PREF_SIZE);

        Label desc = new Label(ressource.getContenu() == null ? "-" : ressource.getContenu());
        desc.setWrapText(true);
        desc.setPrefHeight(60);
        desc.getStyleClass().add("card-subtitle");

        HBox topActions = new HBox(10);
        topActions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().add("btn-primary");
        openBtn.setPrefWidth(120);
        openBtn.setPrefHeight(40);
        openBtn.setOnAction(e -> onOpenResource(ressource));

        Button downloadIconBtn = new Button("📥");
        downloadIconBtn.getStyleClass().add("btn-action-light");
        downloadIconBtn.setStyle("-fx-font-size: 18px; -fx-padding: 5 12; -fx-background-radius: 10;");
        downloadIconBtn.setPrefHeight(40);
        downloadIconBtn.setOnAction(e -> onDownloadResource(ressource));

        topActions.getChildren().addAll(openBtn, downloadIconBtn);

        VBox bottomActions = new VBox(10);
        bottomActions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        try {
            if (quizService.hasQuizForResource(ressource.getId())) {
                Button assessmentBtn = new Button("Start Assessment");
                assessmentBtn.getStyleClass().add("btn-primary");
                assessmentBtn.setPrefWidth(150);
                assessmentBtn.setPrefHeight(36);

                // Vérifier si l'utilisateur a déjà répondu au quiz
                Users quizUser = SessionManager.getInstance().getCurrentUser();
                boolean hasCompleted = false;
                if (quizUser != null) {
                    try {
                        List<Quiz> quizzes = quizService.afficherParRessource(ressource.getId());
                        if (!quizzes.isEmpty()) {
                            hasCompleted = quizResultService.hasUserCompletedQuiz(quizUser.getId(), quizzes.get(0).getId());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if (hasCompleted) {
                    // Bouton gris pour voir l'historique
                    assessmentBtn.setText("View Results");
                    assessmentBtn.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #6b7280; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;");
                    assessmentBtn.setOnAction(e -> onViewQuizResults(ressource));
                } else {
                    // Bouton normal pour commencer le quiz
                    assessmentBtn.setOnAction(e -> onStartAssessment(ressource));
                }

                bottomActions.getChildren().add(assessmentBtn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        card.setMinHeight(230);
        card.setMaxWidth(340);
        card.getChildren().addAll(name, type, desc, topActions);
        if (!bottomActions.getChildren().isEmpty()) {
            card.getChildren().add(bottomActions);
        }
        return card;
    }

    private void onOpenResource(Ressource res) {
        modalResourceTitle.setText(res.getNom());
        modalResourceDesc.setText(res.getContenu() != null ? res.getContenu() : "No details available.");
        resourceModal.setVisible(true);
        new ZoomIn(resourceModal).setSpeed(1.5).play();
        
        updateProgress(res);
    }

    private void updateProgress(Ressource res) {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user != null && currentCourse != null) {
            try {
                progressService.markResourceCompleted(user.getId(), res.getId(), currentCourse.getId());
                // Refresh the list to show the checkmark immediately
                renderResources();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void onStartAssessment(Ressource ressource) {
        openQuizModal(ressource);
    }

    private void onViewQuizResults(Ressource ressource) {
        try {
            Users user = SessionManager.getInstance().getCurrentUser();
            if (user == null) return;

            List<Quiz> quizzes = quizService.afficherParRessource(ressource.getId());
            if (quizzes.isEmpty()) return;

            Quiz quiz = quizzes.get(0);
            QuizResult result = quizResultService.getUserQuizResult(user.getId(), quiz.getId());
            if (result != null) {
                showQuizResultsModal(ressource, quiz, result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Erreur", "Impossible de charger les résultats : " + e.getMessage());
        }
    }

    private void showQuizResultsModal(Ressource ressource, Quiz quiz, QuizResult result) {
        try {
            List<Question> questions = questionService.afficherParQuiz(quiz.getId());

            quizModalTitle.setText("Quiz Results: " + ressource.getNom());
            quizQuestionsContainer.getChildren().clear();

            // Afficher le score global
            Label scoreLabel = new Label(String.format("Your Score: %.0f%%", result.getScore()));
            scoreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " +
                (result.getScore() >= 50 ? "#16a34a" : "#dc2626") + "; -fx-padding: 0 0 20 0;");
            quizQuestionsContainer.getChildren().add(scoreLabel);

            // Afficher chaque question avec la réponse de l'utilisateur
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                Integer userAnswerIndex = result.getAnswers().get(question.getId());

                VBox questionCard = new VBox(10);
                questionCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 16; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-text-fill: #1f2937;");

                Label questionLabel = new Label((i + 1) + ". " + question.getQuestionText());
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

                VBox optionsBox = new VBox(8);
                for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                    String optionText = question.getOptions().get(optionIndex);
                    Label optionLabel = new Label(optionText);
                    optionLabel.setWrapText(true);
                    optionLabel.setStyle("-fx-font-size: 13px; -fx-padding: 8 10; -fx-background-radius: 10;");

                    if (optionIndex == question.getCorrectAnswerIndex()) {
                        // Bonne réponse en vert
                        optionLabel.setStyle(optionLabel.getStyle() + " -fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-background-color: #ecfdf5;");
                    } else if (userAnswerIndex != null && optionIndex == userAnswerIndex) {
                        // Réponse de l'utilisateur incorrecte en rouge
                        optionLabel.setStyle(optionLabel.getStyle() + " -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-background-color: #fef2f2;");
                    } else {
                        optionLabel.setStyle(optionLabel.getStyle() + " -fx-text-fill: #334155;");
                    }

                    optionsBox.getChildren().add(optionLabel);
                }

                questionCard.getChildren().addAll(questionLabel, optionsBox);
                quizQuestionsContainer.getChildren().add(questionCard);
            }

            // Masquer le bouton de soumission et les résultats
            if (quizResultCard != null) {
                quizResultCard.setVisible(false);
                quizResultCard.setManaged(false);
            }
            submitQuizButton.setVisible(false);
            submitQuizButton.setManaged(false);

            quizModal.setVisible(true);
            new animatefx.animation.ZoomIn(quizModal).setSpeed(1.5).play();

        } catch (SQLException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Erreur", "Impossible de charger les détails du quiz : " + e.getMessage());
        }
    }

    private void openQuizModal(Ressource ressource) {
        try {
            Users user = SessionManager.getInstance().getCurrentUser();
            List<Quiz> quizzes = quizService.afficherParRessource(ressource.getId());
            if (quizzes.isEmpty()) {
                gui.util.AlertHelper.showError("No Quiz", "Aucun quiz n'est disponible pour cette ressource.");
                return;
            }

            activeResource = ressource;
            activeQuiz = quizzes.get(0);

            if (user != null && quizResultService.hasUserCompletedQuiz(user.getId(), activeQuiz.getId())) {
                QuizResult existingResult = quizResultService.getUserQuizResult(user.getId(), activeQuiz.getId());
                if (existingResult != null) {
                    showQuizResultsModal(activeResource, activeQuiz, existingResult);
                    return;
                }
            }

            List<Question> questions = questionService.afficherParQuiz(activeQuiz.getId());
            if (questions.isEmpty()) {
                gui.util.AlertHelper.showError("No Questions", "Ce quiz ne contient aucune question.");
                return;
            }

            quizModalTitle.setText("Assessment: " + ressource.getNom());
            quizQuestionsContainer.getChildren().clear();
            if (quizResultCard != null) {
                quizResultCard.setVisible(false);
                quizResultCard.setManaged(false);
            }
            quizResultLabel.setText("");
            submitQuizButton.setVisible(true);
            submitQuizButton.setManaged(true);

            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                VBox questionCard = new VBox(10);
                questionCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 16; -fx-border-radius: 12; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-text-fill: #1f2937;");

                Label questionLabel = new Label((i + 1) + ". " + question.getQuestionText());
                questionLabel.setWrapText(true);
                questionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

                javafx.scene.control.ToggleGroup toggleGroup = new javafx.scene.control.ToggleGroup();
                VBox optionsBox = new VBox(8);
                for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                    String optionText = question.getOptions().get(optionIndex);
                    javafx.scene.control.RadioButton optionButton = new javafx.scene.control.RadioButton(optionText);
                    optionButton.setToggleGroup(toggleGroup);
                    optionButton.setUserData(optionIndex);
                    optionButton.setWrapText(true);
                    optionButton.setStyle("-fx-font-size: 13px; -fx-padding: 8 10; -fx-background-radius: 10; -fx-text-fill: #334155;");
                    optionsBox.getChildren().add(optionButton);
                }

                questionCard.getChildren().addAll(questionLabel, optionsBox);
                quizQuestionsContainer.getChildren().add(questionCard);
            }

            quizModal.setVisible(true);
            new animatefx.animation.ZoomIn(quizModal).setSpeed(1.5).play();
        } catch (SQLException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Erreur", "Impossible de charger le quiz : " + e.getMessage());
        }
    }

    @FXML
    private void submitQuizAnswers() {
        try {
            Users user = SessionManager.getInstance().getCurrentUser();
            if (user == null) {
                gui.util.AlertHelper.showError("Erreur", "Utilisateur non connecté");
                return;
            }

            int questionCount = quizQuestionsContainer.getChildren().size();
            if (questionCount == 0) {
                gui.util.AlertHelper.showError("Erreur", "Aucune question à évaluer.");
                return;
            }

            int correctCount = 0;
            Map<Integer, Integer> userAnswers = new HashMap<>();

            for (int i = 0; i < questionCount; i++) {
                VBox questionCard = (VBox) quizQuestionsContainer.getChildren().get(i);
                VBox optionsBox = (VBox) questionCard.getChildren().get(1);
                int selectedIndex = -1;
                javafx.scene.control.RadioButton selectedButton = null;

                // Trouver la réponse sélectionnée
                for (int j = 0; j < optionsBox.getChildren().size(); j++) {
                    javafx.scene.control.RadioButton optionButton = (javafx.scene.control.RadioButton) optionsBox.getChildren().get(j);
                    if (optionButton.isSelected()) {
                        selectedIndex = (int) optionButton.getUserData();
                        selectedButton = optionButton;
                        break;
                    }
                }

                try {
                    Question question = questionService.afficherParQuiz(activeQuiz.getId()).get(i);
                    userAnswers.put(question.getId(), selectedIndex);
                    boolean isCorrect = selectedIndex == question.getCorrectAnswerIndex();

                    if (isCorrect) {
                        correctCount++;
                        if (selectedButton != null) {
                            selectedButton.setStyle("-fx-font-size: 13px; -fx-padding: 8 10; -fx-background-radius: 10; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        }
                    } else {
                        // Marquer la réponse incorrecte en rouge
                        if (selectedButton != null) {
                            selectedButton.setStyle("-fx-font-size: 13px; -fx-padding: 8 10; -fx-background-radius: 10; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        }

                        // Ajouter l'explication juste en dessous de la réponse choisie
                        String correctOption = question.getOptions().get(question.getCorrectAnswerIndex());
                        String selectedOption = selectedIndex >= 0 && selectedIndex < question.getOptions().size()
                                ? question.getOptions().get(selectedIndex)
                                : "Aucune réponse sélectionnée";

                        Label explanationLabel = new Label(String.format("❌ Incorrect. La bonne réponse est : \"%s\"", correctOption));
                        explanationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-padding: 4 0 0 20; -fx-font-style: italic;");
                        explanationLabel.setWrapText(true);

                        // Insérer l'explication après la réponse sélectionnée
                        int insertIndex = optionsBox.getChildren().indexOf(selectedButton) + 1;
                        optionsBox.getChildren().add(insertIndex, explanationLabel);
                    }

                    // Désactiver tous les boutons radio après soumission
                    for (int j = 0; j < optionsBox.getChildren().size(); j++) {
                        if (optionsBox.getChildren().get(j) instanceof javafx.scene.control.RadioButton) {
                            ((javafx.scene.control.RadioButton) optionsBox.getChildren().get(j)).setDisable(true);
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            double score = questionCount == 0 ? 0 : ((double) correctCount / questionCount) * 100.0;

            // Vérifier si l'utilisateur a déjà un résultat pour ce quiz
            if (quizResultService.hasUserCompletedQuiz(user.getId(), activeQuiz.getId())) {
                QuizResult existingResult = quizResultService.getUserQuizResult(user.getId(), activeQuiz.getId());
                if (existingResult != null) {
                    showQuizResultsModal(activeResource, activeQuiz, existingResult);
                    return;
                }
            }

            // Sauvegarder le résultat
            QuizResult quizResult = new QuizResult(
                user.getId(),
                activeQuiz.getId(),
                score,
                java.time.LocalDateTime.now().toString(),
                userAnswers
            );
            quizResultService.ajouter(quizResult);

            // Afficher les résultats
            boolean success = score >= 50.0;
            String verdict = success ? "Réussi" : "Échoué";
            quizResultIcon.setText(success ? "✔" : "✖");
            quizResultIcon.setStyle(success ? "-fx-text-fill: #16a34a; -fx-font-size: 22px; -fx-font-weight: bold;" : "-fx-text-fill: #dc2626; -fx-font-size: 22px; -fx-font-weight: bold;");
            quizResultLabel.setText(String.format("Score: %.0f%% — %d/%d corrects. %s", score, correctCount, questionCount, verdict));
            quizResultLabel.setStyle(success ? "-fx-text-fill: #0f172a;" : "-fx-text-fill: #0f172a;");
            if (quizResultCard != null) {
                quizResultCard.setStyle(success ? "-fx-background-color: #ecfdf5; -fx-padding: 18; -fx-background-radius: 18; -fx-border-color: transparent; -fx-effect: dropshadow(three-pass-box, rgba(16, 185, 129, 0.15), 18, 0, 0, 10);"
                        : "-fx-background-color: #fef2f2; -fx-padding: 18; -fx-background-radius: 18; -fx-border-color: transparent; -fx-effect: dropshadow(three-pass-box, rgba(239, 68, 68, 0.15), 18, 0, 0, 10);"
                );
                quizResultCard.setVisible(true);
                quizResultCard.setManaged(true);
                new animatefx.animation.FadeIn(quizResultCard).setSpeed(1.5).play();
            }

            submitQuizButton.setVisible(false);
            submitQuizButton.setManaged(false);

        } catch (SQLException e) {
            e.printStackTrace();
            gui.util.AlertHelper.showError("Erreur", "Impossible de sauvegarder les résultats : " + e.getMessage());
        }
    }

    @FXML
    private void closeQuizModal() {
        animatefx.animation.ZoomOut zoomOut = new animatefx.animation.ZoomOut(quizModal);
        zoomOut.setSpeed(1.5);
        zoomOut.setOnFinished(e -> quizModal.setVisible(false));
        zoomOut.play();
    }

    @FXML
    private void closeResourceModal() {
        ZoomOut zoomOut = new ZoomOut(resourceModal);
        zoomOut.setSpeed(1.5);
        zoomOut.setOnFinished(e -> resourceModal.setVisible(false));
        zoomOut.play();
    }

    private void onDownloadResource(Ressource res) {
        String fileUrl = res.getUrl();
        if (fileUrl == null || fileUrl.isEmpty()) {
            System.out.println("No URL for resource: " + res.getNom());
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource");
        
        // Suggest a filename based on URL or name
        String ext = fileUrl.contains(".") ? fileUrl.substring(fileUrl.lastIndexOf(".")) : ".bin";
        if (ext.length() > 5) ext = ".bin"; // Sanitize
        fileChooser.setInitialFileName(res.getNom().replaceAll("[^a-zA-Z0-9.-]", "_") + ext);
        
        File file = fileChooser.showSaveDialog(resourcesContainer.getScene().getWindow());
        
        if (file != null) {
            updateProgress(res);
            new Thread(() -> {
                try (InputStream in = new URL(fileUrl).openStream()) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Platform.runLater(() -> {
                        // Optional: Show success alert
                        System.out.println("Downloaded: " + file.getAbsolutePath());
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }









    @FXML
    private void goBack(javafx.event.ActionEvent event) {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().onShowCourses();
        }
    }

    @javafx.fxml.FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }

    @FXML
    private void openTutorModal() {
        tutorModal.setVisible(true);
        new ZoomIn(tutorModal).setSpeed(1.5).play();
        if (chatBox.getChildren().isEmpty()) {
            addChatBubble("Tutor", "Hi! I'm your AI tutor for " + (currentCourse != null ? currentCourse.getTitre() : "this course") + ". What would you like to know?", false);
        }
    }

    @FXML
    private void closeTutorModal() {
        ZoomOut zoomOut = new ZoomOut(tutorModal);
        zoomOut.setSpeed(1.5);
        zoomOut.setOnFinished(e -> tutorModal.setVisible(false));
        zoomOut.play();
    }

    @FXML
    private void sendChatMessage() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) return;

        addChatBubble("You", msg, true);
        chatInput.clear();

        // Call Gemini API asynchronously
        new Thread(() -> {
            String reply = callGeminiAPI(msg);
            Platform.runLater(() -> addChatBubble("Tutor", reply, false));
        }).start();
    }

    private void addChatBubble(String sender, String text, boolean isUser) {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 12; " + 
                         (isUser ? "-fx-background-color: #ec4899; -fx-text-fill: white;" 
                                 : "-fx-background-color: #f1f5f9; -fx-text-fill: #334155;"));
        
        bubble.getChildren().addAll(senderLabel, msgLabel);
        
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(bubble);
        row.setAlignment(isUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        
        chatBox.getChildren().add(row);
        
        // Auto scroll to bottom
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private String callGeminiAPI(String prompt) {
        try {
            String apiKey = GroqConfig.API_KEY;
            if (apiKey == null || apiKey.isEmpty()) return "Error: API key missing.";
            apiKey = apiKey.trim();
            
            String systemPrompt = "You are a helpful course tutor for " + (currentCourse != null ? currentCourse.getTitre() : "this course") + ". Keep answers concise and professional.";
            
            // Proper JSON escaping for the prompt
            String escapedPrompt = prompt.replace("\\", "\\\\")
                                           .replace("\"", "\\\"")
                                           .replace("\n", "\\n")
                                           .replace("\r", "\\r")
                                           .replace("\t", "\\t");
            
            String jsonPayload = "{" +
                "\"model\": \"llama-3.1-8b-instant\"," +
                "\"messages\": [" +
                    "{\"role\": \"system\", \"content\": \"" + systemPrompt.replace("\"", "\\\"") + "\"}," +
                    "{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}" +
                "]" +
            "}";
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            String body = response.body();
            if (response.statusCode() != 200) {
                return "Groq Error (" + response.statusCode() + "): " + body;
            }

            // Parse response for OpenAI/Groq format: choices[0].message.content
            // Using a more flexible search for "content" field
            int contentKeyIndex = body.indexOf("\"content\"");
            if (contentKeyIndex != -1) {
                int colonIndex = body.indexOf(":", contentKeyIndex);
                if (colonIndex != -1) {
                    int contentStart = body.indexOf("\"", colonIndex);
                    if (contentStart != -1) {
                        contentStart++; // Move past the opening quote
                        int contentEnd = -1;
                        
                        // Find the end quote, skipping escaped ones (\")
                        for (int i = contentStart; i < body.length(); i++) {
                            if (body.charAt(i) == '\"') {
                                // Check if this quote is escaped
                                int backslashCount = 0;
                                for (int j = i - 1; j >= contentStart && body.charAt(j) == '\\'; j--) {
                                    backslashCount++;
                                }
                                if (backslashCount % 2 == 0) {
                                    contentEnd = i;
                                    break;
                                }
                            }
                        }
                        
                        if (contentEnd != -1) {
                            String result = body.substring(contentStart, contentEnd);
                            // Unescape the result
                            result = result.replace("\\n", "\n")
                                         .replace("\\\"", "\"")
                                         .replace("\\\\", "\\")
                                         .replace("\\t", "\t")
                                         .replace("\\r", "\r");
                            return result;
                        }
                    }
                }
            }
            return "Sorry, I couldn't parse the Groq response. Body: " + body;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling AI: " + e.getMessage();
        }
    }
}
