package entities;

import java.time.LocalDateTime;

public class Post {
    private int id;
    private String title;
    private String content;
    private String status;
    private String imageName;
    private String pdfName;
    private int likes;
    private boolean pinned;
    private boolean isCommentLocked;
    private boolean isProfane;
    private boolean isSpam;
    private int spamScore;
    private int profaneWords;
    private int grammarErrors;
    private String refusalReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int userId; // FK reference

    public Post() {
        this.createdAt = LocalDateTime.now();
        this.status = "published";
        this.likes = 0;
        this.pinned = false;
        this.isCommentLocked = false;
        this.isProfane = false;
        this.isSpam = false;
        this.spamScore = 0;
        this.profaneWords = 0;
        this.grammarErrors = 0;
    }

    public Post(int id, String title, String content, String status,
                String imageName, String pdfName, int likes, boolean pinned,
                boolean isCommentLocked, boolean isProfane, boolean isSpam,
                int spamScore, int profaneWords, int grammarErrors,
                String refusalReason, LocalDateTime createdAt,
                LocalDateTime updatedAt, int userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.status = status;
        this.imageName = imageName;
        this.pdfName = pdfName;
        this.likes = likes;
        this.pinned = pinned;
        this.isCommentLocked = isCommentLocked;
        this.isProfane = isProfane;
        this.isSpam = isSpam;
        this.spamScore = spamScore;
        this.profaneWords = profaneWords;
        this.grammarErrors = grammarErrors;
        this.refusalReason = refusalReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getPdfName() { return pdfName; }
    public void setPdfName(String pdfName) { this.pdfName = pdfName; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isCommentLocked() { return isCommentLocked; }
    public void setCommentLocked(boolean commentLocked) { isCommentLocked = commentLocked; }

    public boolean isProfane() { return isProfane; }
    public void setProfane(boolean profane) { isProfane = profane; }

    public boolean isSpam() { return isSpam; }
    public void setSpam(boolean spam) { isSpam = spam; }

    public int getSpamScore() { return spamScore; }
    public void setSpamScore(int spamScore) { this.spamScore = spamScore; }

    public int getProfaneWords() { return profaneWords; }
    public void setProfaneWords(int profaneWords) { this.profaneWords = profaneWords; }

    public int getGrammarErrors() { return grammarErrors; }
    public void setGrammarErrors(int grammarErrors) { this.grammarErrors = grammarErrors; }

    public String getRefusalReason() { return refusalReason; }
    public void setRefusalReason(String refusalReason) { this.refusalReason = refusalReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", likes=" + likes +
                ", userId=" + userId +
                '}';
    }
}

