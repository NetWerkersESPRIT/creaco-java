package gui;

import entities.Question;
import entities.Quiz;
import entities.QuizResult;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuestionService;
import services.QuizResultService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizDialogController {

    @FXML private Label quizTitle;
    @FXML private Label quizSubtitle;
    @FXML private VBox questionsContainer;
    @FXML private VBox resultContainer;
    @FXML private Label resultIcon;
    @FXML private Label resultHeader;
    @FXML private Label resultMessage;
    @FXML private Button submitButton;

    private Quiz quiz;
    private List<Question> questions;
    private final QuestionService questionService = new QuestionService();
    private final QuizResultService quizResultService = new QuizResultService();
    private Runnable onQuizCompleted;

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.quizTitle.setText(quiz.getTitle());
        loadQuestions();
    }

    public void setOnQuizCompleted(Runnable callback) {
        this.onQuizCompleted = callback;
    }

    private void loadQuestions() {
        try {
            questions = questionService.afficherParQuiz(quiz.getId());
            renderQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderQuestions() {
        questionsContainer.getChildren().clear();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            VBox questionBox = new VBox(12);
            questionBox.getStyleClass().add("card");
            questionBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 18; -fx-background-radius: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 15;");

            Label qText = new Label((i + 1) + ". " + q.getQuestionText());
            qText.setWrapText(true);
            qText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

            ToggleGroup group = new ToggleGroup();
            VBox optionsBox = new VBox(8);
            for (int j = 0; j < q.getOptions().size(); j++) {
                RadioButton rb = new RadioButton(q.getOptions().get(j));
                rb.setToggleGroup(group);
                rb.setUserData(j);
                rb.setWrapText(true);
                rb.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
                optionsBox.getChildren().add(rb);
            }

            questionBox.getChildren().addAll(qText, optionsBox);
            questionsContainer.getChildren().add(questionBox);
        }
    }

    @FXML
    private void handleSubmit() {
        Users user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        int correctCount = 0;
        Map<Integer, Integer> userAnswers = new HashMap<>();

        for (int i = 0; i < questionsContainer.getChildren().size(); i++) {
            VBox questionBox = (VBox) questionsContainer.getChildren().get(i);
            VBox optionsBox = (VBox) questionBox.getChildren().get(1);
            ToggleGroup group = ((RadioButton) optionsBox.getChildren().get(0)).getToggleGroup();
            
            RadioButton selected = (RadioButton) group.getSelectedToggle();
            int selectedIndex = (selected != null) ? (int) selected.getUserData() : -1;
            
            Question q = questions.get(i);
            userAnswers.put(q.getId(), selectedIndex);
            if (selectedIndex == q.getCorrectAnswerIndex()) {
                correctCount++;
            }
        }

        double score = ((double) correctCount / questions.size()) * 100.0;
        QuizResult result = new QuizResult(user.getId(), quiz.getId(), score, LocalDateTime.now().toString(), userAnswers);

        try {
            quizResultService.ajouter(result);
            showResults(score, correctCount, questions.size());
            if (onQuizCompleted != null) onQuizCompleted.run();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showResults(double score, int correct, int total) {
        boolean success = score >= 50;
        resultIcon.setText(success ? "✔" : "✖");
        resultIcon.setStyle("-fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + ";");
        resultHeader.setText(success ? "Well Done!" : "Keep Practicing!");
        resultHeader.setStyle("-fx-text-fill: " + (success ? "#16a34a" : "#dc2626") + "; -fx-font-weight: bold;");
        resultMessage.setText(String.format("You scored %.0f%% (%d/%d correct answers).", score, correct, total));
        
        resultContainer.setStyle("-fx-background-color: " + (success ? "#ecfdf5" : "#fef2f2") + ";");
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);
        
        submitButton.setVisible(false);
        submitButton.setManaged(false);
    }

    @FXML
    private void handleClose() {
        ((Stage) questionsContainer.getScene().getWindow()).close();
    }

    public void showAlreadyCompleted(QuizResult result, List<Question> questions) {
        this.questions = questions;
        renderQuestions();
        
        // Disable all radio buttons and highlight correct/wrong answers
        for (int i = 0; i < questionsContainer.getChildren().size(); i++) {
            VBox questionBox = (VBox) questionsContainer.getChildren().get(i);
            VBox optionsBox = (VBox) questionBox.getChildren().get(1);
            Question q = questions.get(i);
            Integer userAnswer = result.getAnswers().get(q.getId());

            for (int j = 0; j < optionsBox.getChildren().size(); j++) {
                RadioButton rb = (RadioButton) optionsBox.getChildren().get(j);
                rb.setDisable(true);
                int optionIndex = (int) rb.getUserData();
                
                if (optionIndex == q.getCorrectAnswerIndex()) {
                    rb.setStyle(rb.getStyle() + " -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                } else if (userAnswer != null && optionIndex == userAnswer) {
                    rb.setStyle(rb.getStyle() + " -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
                
                if (userAnswer != null && optionIndex == userAnswer) {
                    rb.setSelected(true);
                }
            }
        }

        showResults(result.getScore(), (int)(result.getScore() * questions.size() / 100.0), questions.size());
        resultHeader.setText("Past Result");
    }
}
