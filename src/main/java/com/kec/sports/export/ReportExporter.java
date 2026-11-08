package com.kec.sports.export;

import com.kec.sports.model.Athlete;
import com.kec.sports.model.Scholarship;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/*
 * ReportExporter.java
 * Generates official .txt scholarship award letters and evaluation summaries
 * for Kongu Engineering College sports board records.
 */
public class ReportExporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");

    // Generates an official award letter .txt file for an athlete
    public String generateAwardLetter(Athlete athlete, Scholarship scholarship, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "AWARD_LETTER_" + athlete.getRegistrationId() + ".txt";
        File file = new File(dir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("================================================================================\n");
            writer.write("                        KONGU ENGINEERING COLLEGE\n");
            writer.write("                          PERUNDURAI, ERODE\n");
            writer.write("                                \n");
            writer.write("                    SPORTS SCHOLARSHIP AWARD LETTER\n");
            writer.write("================================================================================\n\n");

            writer.write("Date: " + DATE_FORMAT.format(new Date()) + "\n\n");

            writer.write(String.format("Athlete Name       : %s%n", athlete.getStudentName()));
            writer.write(String.format("Registration ID    : %s%n", athlete.getRegistrationId()));
            writer.write(String.format("Department         : %s%n", athlete.getDepartment()));
            writer.write(String.format("Academic Year      : Year %d%n", athlete.getAcademicYear()));
            writer.write(String.format("Semester           : %d%n", athlete.getSemester()));
            writer.write(String.format("Sports Category    : %s%n", athlete.getSportsCategory()));
            writer.write(String.format("Sports Name        : %s%n\n", athlete.getSportsName()));

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write("                         ACADEMIC ELIGIBILITY\n");
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("Academic SGPA      : %.2f%n", athlete.getSgpa()));
            writer.write(String.format("Eligibility Status : %s%n\n", athlete.getEligibilityStatus()));

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write("                       PERFORMANCE EVALUATION\n");
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("Fitness Score      : %.2f/100%n", scholarship.getFitnessScore()));
            writer.write(String.format("Tournament Score   : %.2f/100%n", scholarship.getTournamentScore()));
            if ("TRACK_AND_FIELD".equalsIgnoreCase(athlete.getSportsCategory())) {
                writer.write(String.format("Trial Score        : %.2f/100%n", scholarship.getTrialScore()));
            } else {
                writer.write("Trial Score        : N/A\n");
            }
            writer.write(String.format("Discipline Score   : %.2f/100%n", scholarship.getDisciplineScore()));
            if ("TEAM_SPORTS".equalsIgnoreCase(athlete.getSportsCategory())) {
                writer.write(String.format("Teamwork Score     : %.2f/100%n", scholarship.getTeamworkScore()));
            } else {
                writer.write("Teamwork Score     : N/A\n");
            }
            writer.write(String.format("%nSports Performance Index (SPI) : %.2f%%%n\n", scholarship.getSpi()));

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write("                        SCHOLARSHIP AWARD\n");
            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("Scholarship Category    : %s%n", scholarship.getScholarshipType()));
            writer.write(String.format("Fee Waiver Percentage   : %.2f%%%n\n", scholarship.getFeeWaiver()));

            if (!"ELIGIBLE".equalsIgnoreCase(athlete.getEligibilityStatus())) {
                writer.write("Your academic SGPA does not meet the minimum requirement of 6.0.\n");
                writer.write("Please improve your academic performance to be eligible for sports scholarships.\n");
            } else if (!"NO_AWARD".equalsIgnoreCase(scholarship.getScholarshipType())) {
                writer.write("Congratulations! Your athletic performance has been evaluated successfully.\n");
                writer.write("This scholarship has been awarded based on the institutional sports performance\n");
                writer.write("and academic eligibility criteria.\n");
            } else {
                writer.write("Unfortunately, you did not meet the minimum requirements for a sports scholarship.\n");
                writer.write("Please continue to improve your athletic performance for future evaluations.\n");
            }
            writer.write("\n================================================================================\n");
            writer.write("          This is a system-generated document. No signature required.\n");
            writer.write("================================================================================\n");
        }

        return file.getAbsolutePath();
    }

    public int generateAllAwardLetters(List<Athlete> athletes, List<Scholarship> scholarships, String outputDir) throws IOException {
        int count = 0;
        for (Athlete a : athletes) {
            for (Scholarship s : scholarships) {
                if (s.getAthleteId() == a.getAthleteId()) {
                    generateAwardLetter(a, s, outputDir);
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    public String generateEvaluationSummary(List<Scholarship> scholarships, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "EVALUATION_SUMMARY.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("================================================================================\n");
            writer.write("                  KEC SPORTS SCHOLARSHIP EVALUATION SUMMARY\n");
            writer.write("                        Department of Physical Education\n");
            writer.write("================================================================================\n");
            writer.write("Date: " + DATE_FORMAT.format(new Date()) + "\n\n");

            int total = scholarships.size();
            int gold = 0, silver = 0, bronze = 0, noAward = 0;
            double totalWaiver = 0.0;
            double sumSpi = 0.0;

            for (Scholarship s : scholarships) {
                sumSpi += s.getSpi();
                totalWaiver += s.getFeeWaiver();
                if ("GOLD".equalsIgnoreCase(s.getScholarshipType())) gold++;
                else if ("SILVER".equalsIgnoreCase(s.getScholarshipType())) silver++;
                else if ("BRONZE".equalsIgnoreCase(s.getScholarshipType())) bronze++;
                else noAward++;
            }

            double avgSpi = total > 0 ? (sumSpi / total) : 0.0;

            writer.write(String.format("Total Athletes Evaluated : %d%n", total));
            writer.write(String.format("Average SPI Score        : %.2f%%%n", avgSpi));
            writer.write(String.format("Total Fee Waiver Amount  : %.2f%%%n%n", totalWaiver));

            writer.write("Scholarship Breakdown:\n");
            writer.write(String.format("  - Gold Scholarships   (100%% Waiver) : %d%n", gold));
            writer.write(String.format("  - Silver Scholarships (50%% Waiver)  : %d%n", silver));
            writer.write(String.format("  - Bronze Scholarships (25%% Waiver)  : %d%n", bronze));
            writer.write(String.format("  - No Award / Ineligible              : %d%n%n", noAward));

            writer.write("--------------------------------------------------------------------------------\n");
            writer.write(String.format("%-18s %-20s %-8s %-12s %-10s%n", "REGISTRATION ID", "STUDENT NAME", "SPI", "SCHOLARSHIP", "WAIVER"));
            writer.write("--------------------------------------------------------------------------------\n");

            for (Scholarship s : scholarships) {
                String reg = s.getRegistrationId() != null ? s.getRegistrationId() : ("ATH-" + s.getAthleteId());
                String name = s.getStudentName() != null ? s.getStudentName() : "N/A";
                writer.write(String.format("%-18s %-20s %6.2f%% %-12s %6.2f%%%n",
                        reg,
                        name.length() > 20 ? name.substring(0, 17) + "..." : name,
                        s.getSpi(),
                        s.getScholarshipType(),
                        s.getFeeWaiver()));
            }

            writer.write("================================================================================\n");
        }
        return file.getAbsolutePath();
    }
}
