package entities;

import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private int resourceId;
    private String createdDate;
    private List<Question> questions;

    public Quiz() {}

    public Quiz(String title, int resourceId, String createdDate) {
        this.title = title;
        this.resourceId = resourceId;
        this.createdDate = createdDate;
    }

    public Quiz(int id, String title, int resourceId, String createdDate) {
        this.id = id;
        this.title = title;
        this.resourceId = resourceId;
        this.createdDate = createdDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", resourceId=" + resourceId +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}