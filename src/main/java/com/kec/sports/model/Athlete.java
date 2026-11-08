package com.kec.sports.model;

import java.sql.Timestamp;

public class Athlete {
    private int athleteId;
    private String registrationId;
    private String studentName;
    private String department;
    private int academicYear;
    private int semester;
    private String sportsCategory; // INDIVIDUAL_SPORTS, TEAM_SPORTS, TRACK_AND_FIELD
    private String sportsName;
    private double sgpa;
    private String eligibilityStatus; // ELIGIBLE, INELIGIBLE, PENDING
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Athlete() {}

    public Athlete(String registrationId, String studentName, String department,
                   int academicYear, int semester, String sportsCategory,
                   String sportsName, double sgpa, String eligibilityStatus) {
        this.registrationId = registrationId;
        this.studentName = studentName;
        this.department = department;
        this.academicYear = academicYear;
        this.semester = semester;
        this.sportsCategory = sportsCategory;
        this.sportsName = sportsName;
        this.sgpa = sgpa;
        this.eligibilityStatus = eligibilityStatus;
    }

    public Athlete(int athleteId, String registrationId, String studentName, String department,
                   int academicYear, int semester, String sportsCategory,
                   String sportsName, double sgpa, String eligibilityStatus,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.athleteId = athleteId;
        this.registrationId = registrationId;
        this.studentName = studentName;
        this.department = department;
        this.academicYear = academicYear;
        this.semester = semester;
        this.sportsCategory = sportsCategory;
        this.sportsName = sportsName;
        this.sgpa = sgpa;
        this.eligibilityStatus = eligibilityStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getAthleteId() { return athleteId; }
    public void setAthleteId(int athleteId) { this.athleteId = athleteId; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getSportsCategory() { return sportsCategory; }
    public void setSportsCategory(String sportsCategory) { this.sportsCategory = sportsCategory; }

    public String getSportsName() { return sportsName; }
    public void setSportsName(String sportsName) { this.sportsName = sportsName; }

    public double getSgpa() { return sgpa; }
    public void setSgpa(double sgpa) { this.sgpa = sgpa; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Athlete{" +
                "athleteId=" + athleteId +
                ", registrationId='" + registrationId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", department='" + department + '\'' +
                ", academicYear=" + academicYear +
                ", semester=" + semester +
                ", sportsCategory='" + sportsCategory + '\'' +
                ", sportsName='" + sportsName + '\'' +
                ", sgpa=" + sgpa +
                ", eligibilityStatus='" + eligibilityStatus + '\'' +
                '}';
    }
}
