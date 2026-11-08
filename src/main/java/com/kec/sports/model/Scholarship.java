package com.kec.sports.model;

import java.sql.Timestamp;

public class Scholarship {
    private int scholarshipId;
    private int athleteId;
    private double fitnessScore;
    private double tournamentScore;
    private double trialScore;
    private double disciplineScore;
    private double teamworkScore;
    private double spi; // Sports Performance Index
    private String scholarshipType; // GOLD, SILVER, BRONZE, NO_AWARD
    private double feeWaiver; // 100.0, 50.0, 25.0, 0.0
    private String evaluationStatus; // EVALUATED, PENDING, INELIGIBLE
    private Timestamp evaluationDate;
    private String remarks;

    // Transient fields for display / joined views
    private String registrationId;
    private String studentName;
    private String department;
    private String sportsCategory;
    private String sportsName;
    private double sgpa;

    public Scholarship() {}

    public Scholarship(int athleteId, double fitnessScore, double tournamentScore,
                       double trialScore, double disciplineScore, double teamworkScore,
                       double spi, String scholarshipType, double feeWaiver,
                       String evaluationStatus, String remarks) {
        this.athleteId = athleteId;
        this.fitnessScore = fitnessScore;
        this.tournamentScore = tournamentScore;
        this.trialScore = trialScore;
        this.disciplineScore = disciplineScore;
        this.teamworkScore = teamworkScore;
        this.spi = spi;
        this.scholarshipType = scholarshipType;
        this.feeWaiver = feeWaiver;
        this.evaluationStatus = evaluationStatus;
        this.remarks = remarks;
    }

    public int getScholarshipId() { return scholarshipId; }
    public void setScholarshipId(int scholarshipId) { this.scholarshipId = scholarshipId; }

    public int getAthleteId() { return athleteId; }
    public void setAthleteId(int athleteId) { this.athleteId = athleteId; }

    public double getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(double fitnessScore) { this.fitnessScore = fitnessScore; }

    public double getTournamentScore() { return tournamentScore; }
    public void setTournamentScore(double tournamentScore) { this.tournamentScore = tournamentScore; }

    public double getTrialScore() { return trialScore; }
    public void setTrialScore(double trialScore) { this.trialScore = trialScore; }

    public double getDisciplineScore() { return disciplineScore; }
    public void setDisciplineScore(double disciplineScore) { this.disciplineScore = disciplineScore; }

    public double getTeamworkScore() { return teamworkScore; }
    public void setTeamworkScore(double teamworkScore) { this.teamworkScore = teamworkScore; }

    public double getSpi() { return spi; }
    public void setSpi(double spi) { this.spi = spi; }

    public String getScholarshipType() { return scholarshipType; }
    public void setScholarshipType(String scholarshipType) { this.scholarshipType = scholarshipType; }

    public double getFeeWaiver() { return feeWaiver; }
    public void setFeeWaiver(double feeWaiver) { this.feeWaiver = feeWaiver; }

    public String getEvaluationStatus() { return evaluationStatus; }
    public void setEvaluationStatus(String evaluationStatus) { this.evaluationStatus = evaluationStatus; }

    public Timestamp getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(Timestamp evaluationDate) { this.evaluationDate = evaluationDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getSportsCategory() { return sportsCategory; }
    public void setSportsCategory(String sportsCategory) { this.sportsCategory = sportsCategory; }

    public String getSportsName() { return sportsName; }
    public void setSportsName(String sportsName) { this.sportsName = sportsName; }

    public double getSgpa() { return sgpa; }
    public void setSgpa(double sgpa) { this.sgpa = sgpa; }

    @Override
    public String toString() {
        return "Scholarship{" +
                "scholarshipId=" + scholarshipId +
                ", athleteId=" + athleteId +
                ", spi=" + spi +
                ", scholarshipType='" + scholarshipType + '\'' +
                ", feeWaiver=" + feeWaiver + "%" +
                ", status='" + evaluationStatus + '\'' +
                '}';
    }
}
