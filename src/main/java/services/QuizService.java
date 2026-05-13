package services;

import entities.Quiz;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class QuizService {

    private final Connection con;

    public QuizService() {
        con = MyConnection.getInstance().getConnection();
    }

    public List<Quiz> afficherParRessource(int resourceId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quiz WHERE resource_id = ? ORDER BY created_date DESC, id DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    quizzes.add(mapQuiz(rs));
                }
            }
        }

        return quizzes;
    }

    public void ajouter(Quiz quiz) throws SQLException {
        String sql = "INSERT INTO quiz (title, resource_id, created_date) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setInt(2, quiz.getResourceId());
            ps.setTimestamp(3, toTimestamp(quiz.getCreatedDate()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quiz.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void modifier(Quiz quiz) throws SQLException {
        String sql = "UPDATE quiz SET title = ? WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, quiz.getTitle());
            ps.setInt(2, quiz.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int quizId) throws SQLException {
        // First delete questions
        QuestionService questionService = new QuestionService();
        List<entities.Question> questions = questionService.afficherParQuiz(quizId);
        for (entities.Question q : questions) {
            questionService.supprimer(q.getId());
        }

        String sql = "DELETE FROM quiz WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        }
    }

    public Quiz getById(int quizId) throws SQLException {
        String sql = "SELECT * FROM quiz WHERE id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuiz(rs);
                }
            }
        }

        return null;
    }

    public boolean hasQuizForResource(int resourceId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM quiz WHERE resource_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setTitle(rs.getString("title"));
        quiz.setResourceId(rs.getInt("resource_id"));
        quiz.setCreatedDate(toString(rs.getTimestamp("created_date")));
        return quiz;
    }

    private Timestamp toTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return Timestamp.valueOf(LocalDateTime.now());
        }

        try {
            return Timestamp.valueOf(LocalDateTime.parse(value));
        } catch (DateTimeParseException ignored) {
            return Timestamp.valueOf(LocalDateTime.now());
        }
    }

    private String toString(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }
}