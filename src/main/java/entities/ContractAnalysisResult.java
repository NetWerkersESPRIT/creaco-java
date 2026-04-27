package entities;

import java.util.List;
import java.util.ArrayList;

/**
 * Holds the structured result returned by GroqContractAnalystService.
 * All scores are on a 0-100 scale.
 */
public class ContractAnalysisResult {

    private int clarityScore;
    private int budgetRealismScore;
    private int timelineFeasibilityScore;
    private int overallScore;
    private List<String> flags;
    private boolean success;
    private String errorMessage;

    /** Successful result constructor. */
    public ContractAnalysisResult(int clarityScore, int budgetRealismScore,
                                  int timelineFeasibilityScore, List<String> flags) {
        this.clarityScore = clarityScore;
        this.budgetRealismScore = budgetRealismScore;
        this.timelineFeasibilityScore = timelineFeasibilityScore;
        this.flags = flags != null ? flags : new ArrayList<>();
        this.overallScore = Math.round((clarityScore + budgetRealismScore + timelineFeasibilityScore) / 3.0f);
        this.success = true;
    }

    /** Error result constructor. */
    public ContractAnalysisResult(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.flags = new ArrayList<>();
    }

    public int getClarityScore()            { return clarityScore; }
    public int getBudgetRealismScore()      { return budgetRealismScore; }
    public int getTimelineFeasibilityScore(){ return timelineFeasibilityScore; }
    public int getOverallScore()            { return overallScore; }
    public List<String> getFlags()          { return flags; }
    public boolean isSuccess()              { return success; }
    public String getErrorMessage()         { return errorMessage; }

    /** Converts overall score (0-100) to a 0.0-1.0 double for JavaFX ProgressBar. */
    public double getOverallProgress()            { return overallScore / 100.0; }
    public double getClarityProgress()            { return clarityScore / 100.0; }
    public double getBudgetRealismProgress()      { return budgetRealismScore / 100.0; }
    public double getTimelineFeasibilityProgress(){ return timelineFeasibilityScore / 100.0; }

    /** Returns a human-readable grade label for a 0-100 score. */
    public static String gradeLabel(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 65) return "Good";
        if (score >= 50) return "Moderate";
        if (score >= 35) return "Weak";
        return "Poor";
    }

    /** Returns a CSS hex color matching the score tier (for dynamic coloring). */
    public static String gradeColor(int score) {
        if (score >= 80) return "#10b981"; // green
        if (score >= 65) return "#f59e0b"; // amber
        if (score >= 50) return "#f97316"; // orange
        return "#ef4444";                  // red
    }
}
