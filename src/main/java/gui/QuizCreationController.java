package gui;

import entities.Course;
import entities.Question;
import entities.Quiz;
import entities.Ressource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.QuestionService;
import services.QuizService;
import utils.GroqService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class QuizCreationController {

    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();

    private Ressource ressource;
    private Course course;
    private final List<Question> questions = new ArrayList<>();

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private TextField titleField;

    @FXML
    private Label titleErrorLabel;

    @FXML
    private RadioButton manualRadio;

    @FXML
    private RadioButton aiRadio;

    @FXML
    private ToggleGroup toggleGroup;

    @FXML
    private VBox manualSection;

    @FXML
    private VBox aiSection;

    @FXML
    private VBox questionsContainer;

    @FXML
    private Spinner<Integer> numQuestionsSpinner;

    @FXML
    private Button generateAiButton;

    @FXML
    public void initialize() {
        if (toggleGroup != null) {
            toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                boolean manualSelected = newToggle == manualRadio;
                manualSection.setVisible(manualSelected);
                manualSection.setManaged(manualSelected);
                aiSection.setVisible(!manualSelected);
                aiSection.setManaged(!manualSelected);
            });
        }
    }

    public void setContext(Course course, Ressource ressource) {
        this.course = course;
        this.ressource = ressource;
        if (subtitleLabel != null && ressource != null) {
            subtitleLabel.setText("Create a quiz for " + ressource.getNom());
        }
    }

    @FXML
    private void onCancel() {
        openRessourceList();
    }

    @FXML
    private void onAddQuestion() {
        addQuestionUI(null);
    }

    @FXML
    private void onGenerateAI() {
        if (ressource == null || ressource.getContenu() == null || ressource.getContenu().isBlank()) {
            AlertHelper.showError("No Content", "The resource has no content to generate quiz from.");
            return;
        }

        generateAiButton.setDisable(true);
        generateAiButton.setText("Generating...");

        new Thread(() -> {
            String content = ressource.getContenu();
            int numQuestions = numQuestionsSpinner.getValue();
            String response = GroqService.generateQuiz(content, numQuestions);

            javafx.application.Platform.runLater(() -> {
                generateAiButton.setDisable(false);
                generateAiButton.setText("✨ Generate with AI");

                if (response != null && !response.startsWith("Error") && !response.startsWith("API Error") && !response.startsWith("Failed")) {
                    try {
                        JsonArray jsonArray = parseQuizResponse(response);
                        questions.clear();
                        questionsContainer.getChildren().clear();

                        for (JsonElement element : jsonArray) {
                            JsonObject obj = element.getAsJsonObject();
                            String questionText = obj.get("question").getAsString();
                            JsonArray optionsArray = obj.getAsJsonArray("options");
                            List<String> options = new ArrayList<>();
                            for (JsonElement opt : optionsArray) {
                                options.add(opt.getAsString());
                            }
                            int correctIndex = obj.get("correctAnswer").getAsInt();

                            Question q = new Question(questionText, options, correctIndex);
                            questions.add(q);
                            addQuestionUI(q);
                        }

                        AlertHelper.showInfo("Success", "Quiz generated successfully with " + jsonArray.size() + " questions.");
                    } catch (Exception e) {
                        AlertHelper.showError("Parse Error", "Failed to parse AI response: " + e.getMessage());
                    }
                } else {
                    AlertHelper.showError("AI Error", "Failed to generate quiz: " + response);
                }
            });
        }).start();
    }

    @FXML
    private void onSave() {
        if (!validateForm()) {
            return;
        }

        if (manualRadio.isSelected()) {
            questions.clear();
            for (int i = 0; i < questionsContainer.getChildren().size(); i++) {
                VBox questionBox = (VBox) questionsContainer.getChildren().get(i);
                TextField qField = (TextField) questionBox.getChildren().get(1);
                HBox optionsBox = (HBox) questionBox.getChildren().get(2);
                TextField optA = (TextField) optionsBox.getChildren().get(1);
                TextField optB = (TextField) optionsBox.getChildren().get(3);
                TextField optC = (TextField) optionsBox.getChildren().get(5);
                TextField optD = (TextField) optionsBox.getChildren().get(7);
                HBox bottomBox = (HBox) questionBox.getChildren().get(3);
                ComboBox<String> correctCombo = (ComboBox<String>) bottomBox.getChildren().get(1);

                String questionText = qField.getText().trim();
                List<String> options = List.of(
                    optA.getText().trim(),
                    optB.getText().trim(),
                    optC.getText().trim(),
                    optD.getText().trim()
                );
                int correctIndex = correctCombo.getSelectionModel().getSelectedIndex();

                if (!questionText.isEmpty() && options.stream().noneMatch(String::isEmpty)) {
                    questions.add(new Question(questionText, options, correctIndex));
                }
            }
        }

        if (questions.isEmpty()) {
            AlertHelper.showError("No Questions", "Please add at least one question to the quiz.");
            return;
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(titleField.getText().trim());
        quiz.setResourceId(ressource.getId());
        quiz.setCreatedDate(LocalDateTime.now().toString());

        try {
            quizService.ajouter(quiz);
            for (Question q : questions) {
                q.setQuizId(quiz.getId());
                questionService.ajouter(q);
            }
            AlertHelper.showInfo("Success", "Quiz created successfully!");
            openRessourceList();
        } catch (SQLException e) {
            AlertHelper.showError("Save Error", "Failed to save quiz: " + e.getMessage());
        }
    }

    private void addQuestionUI(Question question) {
        VBox questionBox = new VBox(8);
        questionBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-color: #f8fafc; -fx-padding: 15;");
        questionBox.setPadding(new Insets(15));

        Label questionLabel = new Label("Question " + (questionsContainer.getChildren().size() + 1));
        questionLabel.setStyle("-fx-font-weight: bold;");

        TextField questionField = new TextField();
        questionField.setPromptText("Enter question");
        if (question != null) {
            questionField.setText(question.getQuestionText());
        }

        HBox optionsBox = new HBox(10);
        optionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label optALabel = new Label("A:");
        TextField optAField = new TextField();
        optAField.setPromptText("Option A");
        optAField.setPrefWidth(150);

        Label optBLabel = new Label("B:");
        TextField optBField = new TextField();
        optBField.setPromptText("Option B");
        optBField.setPrefWidth(150);

        Label optCLabel = new Label("C:");
        TextField optCField = new TextField();
        optCField.setPromptText("Option C");
        optCField.setPrefWidth(150);

        Label optDLabel = new Label("D:");
        TextField optDField = new TextField();
        optDField.setPromptText("Option D");
        optDField.setPrefWidth(150);

        optionsBox.getChildren().addAll(optALabel, optAField, optBLabel, optBField, optCLabel, optCField, optDLabel, optDField);

        if (question != null && question.getOptions() != null && question.getOptions().size() >= 4) {
            optAField.setText(question.getOptions().get(0));
            optBField.setText(question.getOptions().get(1));
            optCField.setText(question.getOptions().get(2));
            optDField.setText(question.getOptions().get(3));
        }

        ComboBox<String> correctAnswerCombo = new ComboBox<>();
        correctAnswerCombo.getItems().addAll("A", "B", "C", "D");
        correctAnswerCombo.setPromptText("Correct Answer");
        if (question != null) {
            correctAnswerCombo.getSelectionModel().select(question.getCorrectAnswerIndex());
        } else {
            correctAnswerCombo.getSelectionModel().select(0);
        }

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(e -> {
            questionsContainer.getChildren().remove(questionBox);
            renumberQuestions();
        });

        HBox bottomBox = new HBox(10);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bottomBox.getChildren().addAll(new Label("Correct:"), correctAnswerCombo, removeButton);

        questionBox.getChildren().addAll(questionLabel, questionField, optionsBox, bottomBox);
        questionsContainer.getChildren().add(questionBox);
    }

    private void renumberQuestions() {
        for (int i = 0; i < questionsContainer.getChildren().size(); i++) {
            VBox box = (VBox) questionsContainer.getChildren().get(i);
            Label label = (Label) box.getChildren().get(0);
            label.setText("Question " + (i + 1));
        }
    }

    private boolean validateForm() {
        clearValidationErrors();

        boolean valid = true;
        String title = titleField.getText();
        if (title == null || title.isBlank()) {
            titleErrorLabel.setText("Quiz title is required.");
            titleErrorLabel.setVisible(true);
            titleErrorLabel.setManaged(true);
            valid = false;
        }

        return valid;
    }

    private void clearValidationErrors() {
        titleErrorLabel.setText("");
        titleErrorLabel.setVisible(false);
        titleErrorLabel.setManaged(false);
    }

    private JsonArray parseQuizResponse(String response) throws Exception {
        try {
            JsonElement root = JsonParser.parseString(response);
            if (root.isJsonArray()) {
                return root.getAsJsonArray();
            }
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("choices") && obj.get("choices").isJsonArray()) {
                    JsonArray choices = obj.getAsJsonArray("choices");
                    if (choices.size() > 0) {
                        JsonObject firstChoice = choices.get(0).getAsJsonObject();
                        if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                            JsonObject message = firstChoice.getAsJsonObject("message");
                            if (message.has("content")) {
                                return JsonParser.parseString(cleanJsonArrayString(message.get("content").getAsString())).getAsJsonArray();
                            }
                        }
                        if (firstChoice.has("content")) {
                            return JsonParser.parseString(cleanJsonArrayString(firstChoice.get("content").getAsString())).getAsJsonArray();
                        }
                    }
                }
                if (obj.has("content")) {
                    return JsonParser.parseString(cleanJsonArrayString(obj.get("content").getAsString())).getAsJsonArray();
                }
            }
        } catch (Exception ignored) {
            // fall through to try extraction from plain text
        }

        String cleaned = extractJsonArrayString(response);
        return JsonParser.parseString(cleaned).getAsJsonArray();
    }

    private String extractJsonArrayString(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new IllegalArgumentException("Could not find a JSON array inside the AI response.");
    }

    private String cleanJsonArrayString(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("[")) {
            return trimmed;
        }
        return extractJsonArrayString(trimmed);
    }

    private void openRessourceList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/resource-list-view.fxml"));
            Parent root = loader.load();
            RessourceListController controller = loader.getController();
            controller.setCourse(course);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to return to the resources page.", exception);
        }
    }

    @FXML
    public void goToPreview(javafx.event.ActionEvent event) {
        gui.PreviewHelper.goToPreview(event);
    }

    @FXML
    public void logout(javafx.event.ActionEvent event) {
        gui.SessionHelper.logout(event);
    }
}
