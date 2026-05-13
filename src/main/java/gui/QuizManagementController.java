package gui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Question;
import entities.Quiz;
import entities.Ressource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuestionService;
import services.QuizService;
import utils.GroqService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizManagementController {

    @FXML private Label resourceLabel;
    @FXML private VBox noQuizView;
    @FXML private VBox manageQuizView;
    @FXML private VBox loadingView;
    @FXML private VBox questionForm;
    @FXML private VBox questionsList;
    @FXML private Label formTitle;
    @FXML private TextArea fieldQuestionText;
    @FXML private TextField fieldOption1, fieldOption2, fieldOption3, fieldOption4;
    @FXML private RadioButton radio1, radio2, radio3, radio4;
    @FXML private ToggleGroup correctAnswerGroup;

    private Ressource resource;
    private Quiz currentQuiz;
    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private Question questionToEdit;
    private Runnable onRefreshCard;

    public void setResource(Ressource res) {
        this.resource = res;
        this.resourceLabel.setText("Resource: " + res.getNom());
        loadQuizState();
    }

    public void setOnRefreshCard(Runnable callback) {
        this.onRefreshCard = callback;
    }

    private void loadQuizState() {
        try {
            List<Quiz> quizzes = quizService.afficherParRessource(resource.getId());
            if (quizzes.isEmpty()) {
                noQuizView.setVisible(true);
                manageQuizView.setVisible(false);
            } else {
                currentQuiz = quizzes.get(0);
                noQuizView.setVisible(false);
                manageQuizView.setVisible(true);
                loadQuestions();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadQuestions() {
        try {
            List<Question> questions = questionService.afficherParQuiz(currentQuiz.getId());
            questionsList.getChildren().clear();
            for (int i = 0; i < questions.size(); i++) {
                questionsList.getChildren().add(buildQuestionRow(questions.get(i), i + 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node buildQuestionRow(Question q, int index) {
        HBox row = new HBox(15);
        row.getStyleClass().add("card");
        row.setStyle("-fx-padding: 15; -fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(index + ". " + q.getQuestionText());
        lbl.setWrapText(true);
        lbl.setMaxWidth(450);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-action-light");
        editBtn.setOnAction(e -> handleEditQuestion(q));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.setStyle("-fx-background-radius: 8; -fx-padding: 5 10; -fx-font-size: 11px;");
        deleteBtn.setOnAction(e -> {
            try {
                questionService.supprimer(q.getId());
                loadQuestions();
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        row.getChildren().addAll(lbl, editBtn, deleteBtn);
        return row;
    }

    @FXML
    private void handleAiGenerate() {
        if (resource.getContenu() == null || resource.getContenu().length() < 50) {
            gui.util.AlertHelper.showError("Error", "Content too short for AI generation.");
            return;
        }

        noQuizView.setVisible(false);
        loadingView.setVisible(true);

        new Thread(() -> {
            try {
                String jsonResponse = GroqService.generateQuiz(resource.getContenu(), 5);
                Gson gson = new Gson();
                java.lang.reflect.Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
                ArrayList<Map<String, Object>> quizData = gson.fromJson(jsonResponse, listType);

                if (quizData != null && !quizData.isEmpty()) {
                    Quiz quiz = new Quiz("Quiz: " + resource.getNom(), resource.getId(), LocalDateTime.now().toString());
                    quizService.ajouter(quiz);

                    for (Map<String, Object> qMap : quizData) {
                        String text = (String) qMap.get("question");
                        List<String> options = (List<String>) qMap.get("options");
                        int correctIndex = ((Double) qMap.get("correctAnswer")).intValue();

                        Question question = new Question(text, options, correctIndex);
                        question.setQuizId(quiz.getId());
                        questionService.ajouter(question);
                    }

                    Platform.runLater(() -> {
                        loadingView.setVisible(false);
                        loadQuizState();
                        if (onRefreshCard != null) onRefreshCard.run();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingView.setVisible(false);
                    noQuizView.setVisible(true);
                    gui.util.AlertHelper.showError("AI Error", "Failed to generate quiz: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleCreateManually() {
        try {
            currentQuiz = new Quiz("Quiz: " + resource.getNom(), resource.getId(), LocalDateTime.now().toString());
            quizService.ajouter(currentQuiz);
            noQuizView.setVisible(false);
            manageQuizView.setVisible(true);
            if (onRefreshCard != null) onRefreshCard.run();
            handleAddQuestion();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAddQuestion() {
        questionToEdit = null;
        formTitle.setText("Add New Question");
        clearForm();
        questionForm.setVisible(true);
    }

    private void handleEditQuestion(Question q) {
        questionToEdit = q;
        formTitle.setText("Edit Question");
        fieldQuestionText.setText(q.getQuestionText());
        fieldOption1.setText(q.getOptions().get(0));
        fieldOption2.setText(q.getOptions().get(1));
        fieldOption3.setText(q.getOptions().get(2));
        fieldOption4.setText(q.getOptions().get(3));
        
        int correct = q.getCorrectAnswerIndex();
        if (correct == 0) radio1.setSelected(true);
        else if (correct == 1) radio2.setSelected(true);
        else if (correct == 2) radio3.setSelected(true);
        else if (correct == 3) radio4.setSelected(true);
        
        questionForm.setVisible(true);
    }

    @FXML
    private void handleSaveQuestion() {
        String text = fieldQuestionText.getText();
        List<String> options = List.of(fieldOption1.getText(), fieldOption2.getText(), fieldOption3.getText(), fieldOption4.getText());
        
        int correct = -1;
        if (radio1.isSelected()) correct = 0;
        else if (radio2.isSelected()) correct = 1;
        else if (radio3.isSelected()) correct = 2;
        else if (radio4.isSelected()) correct = 3;

        if (text.isEmpty() || options.stream().anyMatch(String::isEmpty) || correct == -1) {
            gui.util.AlertHelper.showError("Error", "Please fill all fields and select a correct answer.");
            return;
        }

        try {
            if (questionToEdit == null) {
                Question q = new Question(text, options, correct);
                q.setQuizId(currentQuiz.getId());
                questionService.ajouter(q);
            } else {
                questionToEdit.setQuestionText(text);
                questionToEdit.setOptions(options);
                questionToEdit.setCorrectAnswerIndex(correct);
                questionService.modifier(questionToEdit);
            }
            questionForm.setVisible(false);
            loadQuestions();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleCancelForm() {
        questionForm.setVisible(false);
    }

    @FXML
    private void handleDeleteQuiz() {
        if (gui.util.AlertHelper.confirmDelete("Quiz")) {
            try {
                quizService.supprimer(currentQuiz.getId());
                currentQuiz = null;
                if (onRefreshCard != null) onRefreshCard.run();
                loadQuizState();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleClose() {
        if (FrontMainController.getInstance() != null) {
            FrontMainController.getInstance().closeModal();
        } else if (questionsList.getScene() != null) {
            ((Stage) questionsList.getScene().getWindow()).close();
        }
    }

    private void clearForm() {
        fieldQuestionText.clear();
        fieldOption1.clear();
        fieldOption2.clear();
        fieldOption3.clear();
        fieldOption4.clear();
        correctAnswerGroup.selectToggle(null);
    }
}
