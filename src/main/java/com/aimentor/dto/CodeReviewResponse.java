package com.aimentor.dto;


import org.springframework.beans.factory.annotation.Autowired;


public class CodeReviewResponse {
    private String bugs;
    private String improvements;
    private String timeComplexity;
    private String betterApproach;
    private String improvedCode;

    public String getBugs() {
        return bugs;
    }

    public void setBugs(String bugs) {
        this.bugs = bugs;
    }

    public String getImprovements() {
        return improvements;
    }

    public void setImprovements(String improvements) {
        this.improvements = improvements;
    }

    public String getTimeComplexity() {
        return timeComplexity;
    }

    public void setTimeComplexity(String timeComplexity) {
        this.timeComplexity = timeComplexity;
    }

    public String getBetterApproach() {
        return betterApproach;
    }

    public void setBetterApproach(String betterApproach) {
        this.betterApproach = betterApproach;
    }

    public String getImprovedCode() {
        return improvedCode;
    }

    public void setImprovedCode(String improvedCode) {
        this.improvedCode = improvedCode;
    }
}