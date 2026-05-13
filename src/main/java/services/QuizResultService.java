package services;

import entities.QuizResult;
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
import java.util.Map;

public class QuizResultService {

    private final Connection con;
    private final Gson gson = new Gson();

    public QuizResultService() {
        con = MyConnection.getInstance().getConnection();
    }

    public boolean hasUserCompletedQuiz(int userId, int quizId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM quiz_result WHERE user_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void ajouter(QuizResult quizResult) throws SQLException {
        String sql = "INSERT INTO quiz_result (user_id, quiz_id, score, submitted_date, answers) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, quizResult.getUserId());
            ps.setInt(2, quizResult.getQuizId());
            ps.setDouble(3, quizResult.getScore());
            ps.setString(4, quizResult.getSubmittedDate());
            ps.setString(5, gson.toJson(quizResult.getAnswers()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quizResult.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public QuizResult getUserQuizResult(int userId, int quizId) throws SQLException {
        String sql = "SELECT * FROM quiz_result WHERE user_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuizResult(rs);
                }
            }
        }
        return null;
    }

    private QuizResult mapQuizResult(ResultSet rs) throws SQLException {
        QuizResult result = new QuizResult();
        result.setId(rs.getInt("id"));
        result.setUserId(rs.getInt("user_id"));
        result.setQuizId(rs.getInt("quiz_id"));
        result.setScore(rs.getDouble("score"));
        result.setSubmittedDate(rs.getString("submitted_date"));
        String answersJson = rs.getString("answers");
        if (answersJson != null) {
            Type mapType = new TypeToken<Map<Integer, Integer>>(){}.getType();
            result.setAnswers(gson.fromJson(answersJson, mapType));
        }
        return result;
    }
}