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

/**
 * ReportExporter - Generates scholarship award letters as .txt files
 * and summary reports into the reports/ folder.
 */
public class ReportExporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    private static final String DOUBLE_LINE = "================================================================================";
    private static final String SINGLE_LINE = "--------------------------------------------------------------------------------";

    /**
     * Generates a professional TXT scholarship award letter for an athlete.
     * Saved as AWARD_LETTER_{registration_id}.txt in outputDir.
     */
    public String generateAwardLetter(Athlete athlete, Scholarship scholarship, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "AWARD_LETTER_" + athlete.getRegistrationId() + ".txt";
        File file = new File(dir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(DOUBLE_LINE);
            writer.newLine();
            writer.write(centerText("KONGU ENGINEERING COLLEGE", 80));
            writer.newLine();
            writer.write(centerText("PERUNDURAI, ERODE", 80));
            writer.newLine();
            writer.newLine();
            writer.write(centerText("SPORTS SCHOLARSHIP AWARD LETTER", 80));
            writer.newLine();
            writer.write(DOUBLE_LINE);
            writer.newLine();
            writer.newLine();

            writer.write("Date: " + DATE_FORMAT.format(new Date()));
            writer.newLine();
            writer.newLine();

            writer.write(String.format("Athlete Name       : %s\n", athlete.getStudentName()));
            writer.write(String.format("Registration ID    : %s\n", athlete.getRegistrationId()));
            writer.write(String.format("Department         : %s\n", athlete.getDepartment()));
            writer.write(String.format("Academic Year      : Year %d\n", athlete.getAcademicYear()));
            writer.write(String.format("Semester           : %d\n", athlete.getSemester()));
            writer.write(String.format("Sports Category    : %s\n", athlete.getSportsCategory()));
            writer.write(String.format("Sports Name        : %s\n", athlete.getSportsName()));
            writer.newLine();

            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(centerText("ACADEMIC ELIGIBILITY", 80));
            writer.newLine();
            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(String.format("Academic SGPA      : %.2f\n", athlete.getSgpa()));
            writer.write(String.format("Eligibility Status : %s\n", athlete.getEligibilityStatus()));
            writer.newLine();

            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(centerText("PERFORMANCE EVALUATION", 80));
            writer.newLine();
            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(String.format("Fitness Score      : %.2f/100\n", scholarship.getFitnessScore()));
            writer.write(String.format("Tournament Score   : %.2f/100\n", scholarship.getTournamentScore()));
            if ("TRACK_AND_FIELD".equalsIgnoreCase(athlete.getSportsCategory())) {
                writer.write(String.format("Trial Score        : %.2f/100\n", scholarship.getTrialScore()));
            } else {
                writer.write("Trial Score        : N/A\n");
            }
            writer.write(String.format("Discipline Score   : %.2f/100\n", scholarship.getDisciplineScore()));
            if ("TEAM_SPORTS".equalsIgnoreCase(athlete.getSportsCategory())) {
                writer.write(String.format("Teamwork Score     : %.2f/100\n", scholarship.getTeamworkScore()));
            } else {
                writer.write("Teamwork Score     : N/A\n");
            }
            writer.newLine();
            writer.write(String.format("Sports Performance Index (SPI) : %.2f%%\n", scholarship.getSpi()));
            writer.newLine();

            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(centerText("SCHOLARSHIP AWARD", 80));
            writer.newLine();
            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(String.format("Scholarship Category    : %s\n", scholarship.getScholarshipType()));
            writer.write(String.format("Fee Waiver Percentage   : %.2f%%\n", scholarship.getFeeWaiver()));
            writer.newLine();

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
            writer.newLine();

            writer.write(DOUBLE_LINE);
            writer.newLine();
            writer.write(centerText("This is a system-generated document. No signature required.", 80));
            writer.newLine();
            writer.write(DOUBLE_LINE);
            writer.newLine();
        }

        return file.getAbsolutePath();
    }

    /**
     * Generates award letters for a list of athletes and scholarships.
     */
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

    /**
     * Generates an institutional evaluation summary report.
     */
    public String generateEvaluationSummary(List<Scholarship> scholarships, String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "EVALUATION_SUMMARY.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(DOUBLE_LINE);
            writer.newLine();
            writer.write(centerText("KEC SPORTS SCHOLARSHIP EVALUATION SUMMARY", 80));
            writer.newLine();
            writer.write(DOUBLE_LINE);
            writer.newLine();
            writer.write("Date: " + DATE_FORMAT.format(new Date()));
            writer.newLine();
            writer.newLine();

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

            writer.write(String.format("Total Athletes Evaluated : %d\n", total));
            writer.write(String.format("Average SPI Score        : %.2f%%\n", avgSpi));
            writer.write(String.format("Total Fee Waiver Amount  : %.2f%%\n", totalWaiver));
            writer.newLine();
            writer.write("Scholarship Distribution:\n");
            writer.write(String.format("  - Gold Scholarships   (100%% Waiver) : %d\n", gold));
            writer.write(String.format("  - Silver Scholarships (50%% Waiver)  : %d\n", silver));
            writer.write(String.format("  - Bronze Scholarships (25%% Waiver)  : %d\n", bronze));
            writer.write(String.format("  - No Award / Ineligible              : %d\n", noAward));
            writer.newLine();

            writer.write(SINGLE_LINE);
            writer.newLine();
            writer.write(String.format("%-18s %-20s %-8s %-12s %-10s\n", "REGISTRATION ID", "STUDENT NAME", "SPI", "SCHOLARSHIP", "WAIVER"));
            writer.write(SINGLE_LINE);
            writer.newLine();

            for (Scholarship s : scholarships) {
                String reg = s.getRegistrationId() != null ? s.getRegistrationId() : ("ATH-" + s.getAthleteId());
                String name = s.getStudentName() != null ? s.getStudentName() : "N/A";
                writer.write(String.format("%-18s %-20s %6.2f%% %-12s %6.2f%%\n",
                        reg,
                        name.length() > 20 ? name.substring(0, 17) + "..." : name,
                        s.getSpi(),
                        s.getScholarshipType(),
                        s.getFeeWaiver()));
            }

            writer.write(DOUBLE_LINE);
            writer.newLine();
        }
        return file.getAbsolutePath();
    }

    private static String centerText(String text, int width) {
        if (text == null) return "";
        int padding = (width - text.length()) / 2;
        if (padding <= 0) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(text);
        return sb.toString();
    }
}
