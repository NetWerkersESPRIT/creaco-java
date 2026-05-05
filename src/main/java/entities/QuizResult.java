package entities;

import java.util.Map;

public class QuizResult {
    private int id;
    private int userId;
    private int quizId;
    private double score;
    private String submittedDate;
    private Map<Integer, Integer> answers; // questionId -> selectedOptionIndex

    public QuizResult() {}

    public QuizResult(int userId, int quizId, double score, String submittedDate, Map<Integer, Integer> answers) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.submittedDate = submittedDate;
        this.answers = answers;
    }

    public QuizResult(int id, int userId, int quizId, double score, String submittedDate, Map<Integer, Integer> answers) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.submittedDate = submittedDate;
        this.answers = answers;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(String submittedDate) {
        this.submittedDate = submittedDate;
    }

    public Map<Integer, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, Integer> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "QuizResult{" +
                "id=" + id +
                ", userId=" + userId +
                ", quizId=" + quizId +
                ", score=" + score +
                ", submittedDate='" + submittedDate + '\'' +
                ", answers=" + answers +
                '}';
    }
}