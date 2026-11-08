package com.kec.sports.model;

import java.sql.Timestamp;

public class TrialLog {
    private int trialId;
    private int athleteId;
    private String registrationId; // transient / helper for CSV import
    private String eventName;
    private String trialTiming; // Format MM:SS.ms (e.g., "00:11.25" or "02:15.45")
    private double normalizedTrialScore;
    private double disciplineScore;
    private String coachRemarks;
    private Timestamp createdAt;

    public TrialLog() {}

    public TrialLog(String registrationId, String eventName, String trialTiming,
                    double disciplineScore, String coachRemarks) {
        this.registrationId = registrationId;
        this.eventName = eventName;
        this.trialTiming = trialTiming;
        this.disciplineScore = disciplineScore;
        this.coachRemarks = coachRemarks;
    }

    public TrialLog(int athleteId, String eventName, String trialTiming,
                    double normalizedTrialScore, double disciplineScore, String coachRemarks) {
        this.athleteId = athleteId;
        this.eventName = eventName;
        this.trialTiming = trialTiming;
        this.normalizedTrialScore = normalizedTrialScore;
        this.disciplineScore = disciplineScore;
        this.coachRemarks = coachRemarks;
    }

    public TrialLog(int trialId, int athleteId, String eventName, String trialTiming,
                    double normalizedTrialScore, double disciplineScore,
                    String coachRemarks, Timestamp createdAt) {
        this.trialId = trialId;
        this.athleteId = athleteId;
        this.eventName = eventName;
        this.trialTiming = trialTiming;
        this.normalizedTrialScore = normalizedTrialScore;
        this.disciplineScore = disciplineScore;
        this.coachRemarks = coachRemarks;
        this.createdAt = createdAt;
    }

    public int getTrialId() { return trialId; }
    public void setTrialId(int trialId) { this.trialId = trialId; }

    public int getAthleteId() { return athleteId; }
    public void setAthleteId(int athleteId) { this.athleteId = athleteId; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getTrialTiming() { return trialTiming; }
    public void setTrialTiming(String trialTiming) { this.trialTiming = trialTiming; }

    public double getNormalizedTrialScore() { return normalizedTrialScore; }
    public void setNormalizedTrialScore(double normalizedTrialScore) { this.normalizedTrialScore = normalizedTrialScore; }

    public double getDisciplineScore() { return disciplineScore; }
    public void setDisciplineScore(double disciplineScore) { this.disciplineScore = disciplineScore; }

    public String getCoachRemarks() { return coachRemarks; }
    public void setCoachRemarks(String coachRemarks) { this.coachRemarks = coachRemarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "TrialLog{" +
                "trialId=" + trialId +
                ", athleteId=" + athleteId +
                ", registrationId='" + registrationId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", trialTiming='" + trialTiming + '\'' +
                ", normalizedTrialScore=" + normalizedTrialScore +
                ", disciplineScore=" + disciplineScore +
                ", coachRemarks='" + coachRemarks + '\'' +
                '}';
    }
}
