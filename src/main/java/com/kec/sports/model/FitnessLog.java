package com.kec.sports.model;

import java.sql.Date;
import java.sql.Timestamp;

public class FitnessLog {
    private int fitnessId;
    private int athleteId;
    private double fitnessScore;
    private double enduranceScore;
    private Date recordedDate;
    private Timestamp createdAt;

    public FitnessLog() {}

    public FitnessLog(int athleteId, double fitnessScore, double enduranceScore, Date recordedDate) {
        this.athleteId = athleteId;
        this.fitnessScore = fitnessScore;
        this.enduranceScore = enduranceScore;
        this.recordedDate = recordedDate;
    }

    public FitnessLog(int fitnessId, int athleteId, double fitnessScore, double enduranceScore, Date recordedDate, Timestamp createdAt) {
        this.fitnessId = fitnessId;
        this.athleteId = athleteId;
        this.fitnessScore = fitnessScore;
        this.enduranceScore = enduranceScore;
        this.recordedDate = recordedDate;
        this.createdAt = createdAt;
    }

    public int getFitnessId() { return fitnessId; }
    public void setFitnessId(int fitnessId) { this.fitnessId = fitnessId; }

    public int getAthleteId() { return athleteId; }
    public void setAthleteId(int athleteId) { this.athleteId = athleteId; }

    public double getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(double fitnessScore) { this.fitnessScore = fitnessScore; }

    public double getEnduranceScore() { return enduranceScore; }
    public void setEnduranceScore(double enduranceScore) { this.enduranceScore = enduranceScore; }

    public Date getRecordedDate() { return recordedDate; }
    public void setRecordedDate(Date recordedDate) { this.recordedDate = recordedDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "FitnessLog{" +
                "fitnessId=" + fitnessId +
                ", athleteId=" + athleteId +
                ", fitnessScore=" + fitnessScore +
                ", enduranceScore=" + enduranceScore +
                ", recordedDate=" + recordedDate +
                '}';
    }
}
