package services;

import entities.Question;
import utils.MyConnection;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private final Connection con;
    private final Gson gson = new Gson();

    public QuestionService() {
        con = MyConnection.getInstance().getConnection();
        createTableIfNotExist();
    }

    private void createTableIfNotExist() {
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS question (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "quiz_id INT NOT NULL, " +
                    "question_text TEXT NOT NULL, " +
                    "options TEXT NOT NULL, " +
                    "correct_answer_index INT NOT NULL)");
        } catch (SQLException e) {
            System.err.println("Error creating question table: " + e.getMessage());
        }
    }

    public List<Question> afficherParQuiz(int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM question WHERE quiz_id = ? ORDER BY id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapQuestion(rs));
                }
            }
        }

        return questions;
    }

    public void ajouter(Question question) throws SQLException {
        String sql = "INSERT INTO question (quiz_id, question_text, options, correct_answer_index) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, gson.toJson(question.getOptions()));
            ps.setInt(4, question.getCorrectAnswerIndex());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void modifier(Question question) throws SQLException {
        String sql = "UPDATE question SET question_text = ?, options = ?, correct_answer_index = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, question.getQuestionText());
            ps.setString(2, gson.toJson(question.getOptions()));
            ps.setInt(3, question.getCorrectAnswerIndex());
            ps.setInt(4, question.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int questionId) throws SQLException {
        String sql = "DELETE FROM question WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        }
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setQuizId(rs.getInt("quiz_id"));
        question.setQuestionText(rs.getString("question_text"));
        String optionsJson = rs.getString("options");
        if (optionsJson != null) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            question.setOptions(gson.fromJson(optionsJson, listType));
        }
        question.setCorrectAnswerIndex(rs.getInt("correct_answer_index"));
        return question;
    }
}