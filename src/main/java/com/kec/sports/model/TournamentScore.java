package com.kec.sports.model;

import java.sql.Timestamp;

public class TournamentScore {
    private int tournamentId;
    private int athleteId;
    private String tournamentName;
    private String tournamentLevel; // ZONAL, INTER_COLLEGE, STATE, NATIONAL
    private double tournamentPoints;
    private String achievement;
    private double teamworkScore;
    private Timestamp createdAt;

    public TournamentScore() {}

    public TournamentScore(int athleteId, String tournamentName, String tournamentLevel,
                           double tournamentPoints, String achievement, double teamworkScore) {
        this.athleteId = athleteId;
        this.tournamentName = tournamentName;
        this.tournamentLevel = tournamentLevel;
        this.tournamentPoints = tournamentPoints;
        this.achievement = achievement;
        this.teamworkScore = teamworkScore;
    }

    public TournamentScore(int tournamentId, int athleteId, String tournamentName,
                           String tournamentLevel, double tournamentPoints,
                           String achievement, double teamworkScore, Timestamp createdAt) {
        this.tournamentId = tournamentId;
        this.athleteId = athleteId;
        this.tournamentName = tournamentName;
        this.tournamentLevel = tournamentLevel;
        this.tournamentPoints = tournamentPoints;
        this.achievement = achievement;
        this.teamworkScore = teamworkScore;
        this.createdAt = createdAt;
    }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public int getAthleteId() { return athleteId; }
    public void setAthleteId(int athleteId) { this.athleteId = athleteId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getTournamentLevel() { return tournamentLevel; }
    public void setTournamentLevel(String tournamentLevel) { this.tournamentLevel = tournamentLevel; }

    public double getTournamentPoints() { return tournamentPoints; }
    public void setTournamentPoints(double tournamentPoints) { this.tournamentPoints = tournamentPoints; }

    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }

    public double getTeamworkScore() { return teamworkScore; }
    public void setTeamworkScore(double teamworkScore) { this.teamworkScore = teamworkScore; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "TournamentScore{" +
                "tournamentId=" + tournamentId +
                ", athleteId=" + athleteId +
                ", tournamentName='" + tournamentName + '\'' +
                ", tournamentLevel='" + tournamentLevel + '\'' +
                ", tournamentPoints=" + tournamentPoints +
                ", achievement='" + achievement + '\'' +
                ", teamworkScore=" + teamworkScore +
                '}';
    }
}
