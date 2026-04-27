package entities.forum;

public class SentimentResult {
    private String label;
    private double score;

    public SentimentResult(String label, double score) {
        this.label = label;
        this.score = score;
    }

    public String getLabel() { return label; }
    public double getScore() { return score; }

    @Override
    public String toString() {
        return label + " (" + (int)(score * 100) + "%)";
    }
}
