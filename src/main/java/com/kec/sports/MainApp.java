package com.kec.sports;

import com.kec.sports.dao.SportsDAO;
import com.kec.sports.evaluation.EvaluationEngine;
import com.kec.sports.export.ReportExporter;
import com.kec.sports.model.*;
import com.kec.sports.util.CSVHandler;
import com.kec.sports.util.StringProcessor;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * MainApp - Interactive Console Application for KEC Student Sports Athletic
 * Performance & Scholarship Evaluator.
 */
public class MainApp {

    private static final String REPORTS_DIR = "reports";
    private static final SportsDAO dao = new SportsDAO();
    private static final EvaluationEngine engine = new EvaluationEngine(dao);
    private static final ReportExporter exporter = new ReportExporter();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("================================================================");
        System.out.println("   KONGU ENGINEERING COLLEGE - SPORTS SCHOLARSHIP EVALUATOR");
        System.out.println("================================================================");

        // Verify DB Connection
        boolean dbOk = DBConnection.getInstance().testConnection();
        if (dbOk) {
            System.out.println("[MainApp] Database connection validated successfully.");
        } else {
            System.err.println("[MainApp] Notice: Database connection could not be established.");
            System.err.println("[MainApp] Please verify application.properties or run OCI VM 2 MySQL.");
        }

        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("Enter your choice (1-15): ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        registerNewAthlete(scanner);
                        break;
                    case 2:
                        recordFitnessScores(scanner);
                        break;
                    case 3:
                        recordTournamentScores(scanner);
                        break;
                    case 4:
                        recordTrialTimings(scanner);
                        break;
                    case 5:
                        importTrialTimingsCSV(scanner);
                        break;
                    case 6:
                        viewAllAthletes();
                        break;
                    case 7:
                        checkAcademicEligibility(scanner);
                        break;
                    case 8:
                        evaluateSingleAthlete(scanner);
                        break;
                    case 9:
                        evaluateAllAthletesMultithreaded();
                        break;
                    case 10:
                        generateSingleAwardLetter(scanner);
                        break;
                    case 11:
                        generateAllAwardLetters();
                        break;
                    case 12:
                        viewScholarshipSummary();
                        break;
                    case 13:
                        searchAthleteByRegId(scanner);
                        break;
                    case 14:
                        testDatabaseConnection();
                        break;
                    case 15:
                        System.out.println("\nExiting KEC Sports Evaluator. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose from 1 to 15.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric choice.");
            } catch (Exception e) {
                System.err.println("[MainApp] Error: " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n================================================================");
        System.out.println("                  MAIN APPLICATION MENU");
        System.out.println("================================================================");
        System.out.println(" 1. Register New Athlete (Phase 1)");
        System.out.println(" 2. Record Physical Fitness & Endurance (Phase 1)");
        System.out.println(" 3. Record Tournament Performance (Phase 2)");
        System.out.println(" 4. Record Trial Timings & Discipline (Phase 3)");
        System.out.println(" 5. Import Trial Timings from CSV");
        System.out.println(" 6. View All Athletes");
        System.out.println(" 7. Check Academic Eligibility (SGPA Filter)");
        System.out.println(" 8. Evaluate Single Athlete");
        System.out.println(" 9. Evaluate All Athletes (Multithreaded Parallel Execution)");
        System.out.println(" 10. Generate Scholarship Award Letter (.txt)");
        System.out.println(" 11. Generate All Award Letters & Summary Report");
        System.out.println(" 12. View Scholarship Summary & Fee Waiver Total");
        System.out.println(" 13. Search Athlete by Registration ID");
        System.out.println(" 14. Test Database Connection");
        System.out.println(" 15. Exit");
        System.out.println("================================================================");
    }

    private static void registerNewAthlete(Scanner scanner) {
        System.out.println("\n--- [Phase 1: Athlete Registration] ---");
        System.out.print("Student Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Department (e.g. CSE, ECE, MECH, EEE, IT, CIVIL): ");
        String dept = scanner.nextLine().trim().toUpperCase();

        System.out.print("Academic Year (1-4): ");
        int year = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Semester (1-8): ");
        int sem = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Select Sports Category:");
        System.out.println("  1. INDIVIDUAL_SPORTS");
        System.out.println("  2. TEAM_SPORTS");
        System.out.println("  3. TRACK_AND_FIELD");
        System.out.print("Choice (1-3): ");
        int catChoice = Integer.parseInt(scanner.nextLine().trim());
        String category = "INDIVIDUAL_SPORTS";
        if (catChoice == 2) category = "TEAM_SPORTS";
        else if (catChoice == 3) category = "TRACK_AND_FIELD";

        System.out.print("Sport Name (e.g. Badminton, Cricket, 100m Sprint): ");
        String sportName = scanner.nextLine().trim();

        System.out.print("Academic SGPA (0.00 - 10.00): ");
        double sgpa = Double.parseDouble(scanner.nextLine().trim());

        int currentYear = 2026;
        int nextSeq = dao.getNextSequenceNumber(currentYear);
        String regId = StringProcessor.generateRegistrationId(nextSeq, currentYear);

        String eligStatus = (sgpa >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";

        Athlete athlete = new Athlete(regId, name, dept, year, sem, category, sportName, sgpa, eligStatus);
        int athleteId = dao.insertAthlete(athlete);

        if (athleteId > 0) {
            System.out.println("[SUCCESS] Athlete Registered Successfully!");
            System.out.println("  Athlete ID      : " + athleteId);
            System.out.println("  Registration ID : " + regId);
            System.out.println("  Eligibility     : " + eligStatus);

            // Optional immediate Phase 1 Fitness Log
            System.out.print("Would you like to enter Fitness & Endurance scores now? (y/n): ");
            String ans = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(ans)) {
                System.out.print("Fitness Score (0-100): ");
                double fitScore = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Endurance Score (0-100): ");
                double endScore = Double.parseDouble(scanner.nextLine().trim());
                FitnessLog log = new FitnessLog(athleteId, fitScore, endScore, new Date(System.currentTimeMillis()));
                dao.insertFitnessLog(log);
                System.out.println("[SUCCESS] Fitness scores recorded for " + regId);
            }
        } else {
            System.err.println("[FAILED] Could not register athlete.");
        }
    }

    private static void recordFitnessScores(Scanner scanner) {
        System.out.println("\n--- [Phase 1: Record Fitness Scores] ---");
        System.out.print("Enter Athlete Registration ID (e.g. KEC-SPT-2026-001): ");
        String regId = scanner.nextLine().trim();

        if (!StringProcessor.validateRegistrationId(regId)) {
            System.err.println("Invalid Registration ID format!");
            return;
        }

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found with Registration ID: " + regId);
            return;
        }

        System.out.println("Athlete: " + athlete.getStudentName() + " (" + athlete.getSportsName() + ")");
        System.out.print("Fitness Score (0-100): ");
        double fitScore = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Endurance Score (0-100): ");
        double endScore = Double.parseDouble(scanner.nextLine().trim());

        FitnessLog log = new FitnessLog(athlete.getAthleteId(), fitScore, endScore, new Date(System.currentTimeMillis()));
        int id = dao.insertFitnessLog(log);
        if (id > 0) {
            System.out.println("[SUCCESS] Fitness log recorded (ID: " + id + ")");
        } else {
            System.err.println("[FAILED] Could not save fitness log.");
        }
    }

    private static void recordTournamentScores(Scanner scanner) {
        System.out.println("\n--- [Phase 2: Record Tournament Performance] ---");
        System.out.print("Enter Athlete Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found with Registration ID: " + regId);
            return;
        }

        System.out.println("Athlete: " + athlete.getStudentName() + " | Category: " + athlete.getSportsCategory());
        System.out.print("Tournament Name: ");
        String tournName = scanner.nextLine().trim();

        System.out.println("Tournament Level:");
        System.out.println("  1. ZONAL\n  2. INTER_COLLEGE\n  3. STATE\n  4. NATIONAL");
        System.out.print("Choice (1-4): ");
        int lvl = Integer.parseInt(scanner.nextLine().trim());
        String level = "ZONAL";
        if (lvl == 2) level = "INTER_COLLEGE";
        else if (lvl == 3) level = "STATE";
        else if (lvl == 4) level = "NATIONAL";

        System.out.print("Tournament Points (0-100): ");
        double points = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Achievement / Position (e.g. Gold Medalist, Runner-Up, Semi-Finalist): ");
        String ach = scanner.nextLine().trim();

        double teamworkScore = 0.0;
        if ("TEAM_SPORTS".equalsIgnoreCase(athlete.getSportsCategory())) {
            System.out.print("Teamwork Score (0-100): ");
            teamworkScore = Double.parseDouble(scanner.nextLine().trim());
        }

        TournamentScore score = new TournamentScore(athlete.getAthleteId(), tournName, level, points, ach, teamworkScore);
        int id = dao.insertTournamentScore(score);
        if (id > 0) {
            System.out.println("[SUCCESS] Tournament score recorded successfully (ID: " + id + ")");
        } else {
            System.err.println("[FAILED] Could not record tournament score.");
        }
    }

    private static void recordTrialTimings(Scanner scanner) {
        System.out.println("\n--- [Phase 3: Record Trial Timings & Discipline] ---");
        System.out.print("Enter Athlete Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        System.out.print("Trial Event Name (e.g. 100m Sprint, 400m Relay): ");
        String event = scanner.nextLine().trim();

        System.out.print("Trial Timing (Format MM:SS.ms, e.g. 00:11.25 or 02:15.45): ");
        String timing = scanner.nextLine().trim();

        if (!StringProcessor.validateTrialTiming(timing)) {
            System.err.println("Invalid timing format! Expected MM:SS.ms");
            return;
        }

        double actualSec = StringProcessor.parseTrialTiming(timing);
        double benchmarkSec = StringProcessor.getBenchmarkTime(event);
        double normalizedScore = StringProcessor.normalizeTrialScore(actualSec, benchmarkSec);

        System.out.printf("  Calculated Time: %.2f s | Benchmark: %.2f s | Normalized Score: %.2f/100\n",
                actualSec, benchmarkSec, normalizedScore);

        System.out.print("Discipline Score (0-100): ");
        double discScore = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Coach Remarks: ");
        String remarks = StringProcessor.sanitizeCoachRemarks(scanner.nextLine().trim());

        TrialLog log = new TrialLog(athlete.getAthleteId(), event, timing, normalizedScore, discScore, remarks);
        int id = dao.insertTrialLog(log);
        if (id > 0) {
            System.out.println("[SUCCESS] Trial timing log recorded successfully (ID: " + id + ")");
        } else {
            System.err.println("[FAILED] Could not save trial log.");
        }
    }

    private static void importTrialTimingsCSV(Scanner scanner) {
        System.out.println("\n--- [File I/O: Import Trial Timings from CSV] ---");
        System.out.print("Enter CSV File Path (default: uploads/trial_timings.csv): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "uploads/trial_timings.csv";
        }

        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File does not exist at: " + file.getAbsolutePath());
            return;
        }

        List<TrialLog> importedLogs = CSVHandler.importTrialTimings(path);
        System.out.println("Parsed " + importedLogs.size() + " records from CSV.");

        int count = 0;
        for (TrialLog log : importedLogs) {
            if (log.getRegistrationId() != null) {
                Athlete athlete = dao.getAthleteByRegistrationId(log.getRegistrationId());
                if (athlete != null) {
                    log.setAthleteId(athlete.getAthleteId());
                    // Compute normalized score using benchmark
                    double actualSec = StringProcessor.parseTrialTiming(log.getTrialTiming());
                    double bench = StringProcessor.getBenchmarkTime(log.getEventName());
                    log.setNormalizedTrialScore(StringProcessor.normalizeTrialScore(actualSec, bench));

                    int id = dao.insertTrialLog(log);
                    if (id > 0) count++;
                } else {
                    System.err.println("Skipping row: Athlete registration ID not found in DB: " + log.getRegistrationId());
                }
            }
        }
        System.out.println("[SUCCESS] Imported and saved " + count + " trial records into MySQL database.");
    }

    private static void viewAllAthletes() {
        System.out.println("\n--- [Registered Student Athletes] ---");
        List<Athlete> list = dao.getAllAthletes();
        if (list.isEmpty()) {
            System.out.println("No athletes registered yet.");
            return;
        }

        System.out.printf("%-4s %-18s %-20s %-6s %-18s %-15s %-5s %-10s\n",
                "ID", "REGISTRATION ID", "STUDENT NAME", "DEPT", "CATEGORY", "SPORT", "SGPA", "STATUS");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Athlete a : list) {
            System.out.printf("%-4d %-18s %-20s %-6s %-18s %-15s %5.2f %-10s\n",
                    a.getAthleteId(),
                    a.getRegistrationId(),
                    a.getStudentName(),
                    a.getDepartment(),
                    a.getSportsCategory(),
                    a.getSportsName(),
                    a.getSgpa(),
                    a.getEligibilityStatus());
        }
    }

    private static void checkAcademicEligibility(Scanner scanner) {
        System.out.println("\n--- [Academic Eligibility Verification] ---");
        System.out.print("Enter Athlete Registration ID (or press Enter to check all): ");
        String regId = scanner.nextLine().trim();

        if (!regId.isEmpty()) {
            Athlete a = dao.getAthleteByRegistrationId(regId);
            if (a == null) {
                System.err.println("Athlete not found: " + regId);
                return;
            }
            String status = (a.getSgpa() >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";
            dao.updateEligibilityStatus(a.getAthleteId(), status);
            System.out.printf("Athlete: %s | SGPA: %.2f | Status: %s\n",
                    a.getStudentName(), a.getSgpa(), status);
            if ("INELIGIBLE".equals(status)) {
                System.out.println("Reason: SGPA < 6.0 marks athlete as INELIGIBLE. Scholarship is revoked.");
            }
        } else {
            List<Athlete> all = dao.getAllAthletes();
            int eligibleCount = 0, ineligibleCount = 0;
            for (Athlete a : all) {
                String status = (a.getSgpa() >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";
                dao.updateEligibilityStatus(a.getAthleteId(), status);
                if ("ELIGIBLE".equals(status)) eligibleCount++;
                else ineligibleCount++;
            }
            System.out.println("Updated academic eligibility for " + all.size() + " athletes.");
            System.out.println("Eligible Athletes   : " + eligibleCount);
            System.out.println("Ineligible Athletes : " + ineligibleCount + " (SGPA < 6.0)");
        }
    }

    private static void evaluateSingleAthlete(Scanner scanner) {
        System.out.println("\n--- [Evaluate Single Athlete] ---");
        System.out.print("Enter Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        Scholarship s = engine.evaluateSingleAthlete(athlete.getAthleteId());
        if (s != null) {
            System.out.println("\n>>> Evaluation Completed:");
            System.out.println("  Athlete          : " + athlete.getStudentName());
            System.out.println("  Sports Category  : " + athlete.getSportsCategory());
            System.out.println("  Academic SGPA    : " + athlete.getSgpa() + " (" + athlete.getEligibilityStatus() + ")");
            System.out.println("  Calculated SPI   : " + s.getSpi() + "%");
            System.out.println("  Scholarship Tier : " + s.getScholarshipType());
            System.out.println("  Fee Waiver       : " + s.getFeeWaiver() + "%");
            System.out.println("  Remarks          : " + s.getRemarks());
        }
    }

    private static void evaluateAllAthletesMultithreaded() {
        List<Scholarship> results = engine.evaluateAllAthletes();
        System.out.printf("\nEvaluated %d athletes successfully in parallel.\n", results.size());
    }

    private static void generateSingleAwardLetter(Scanner scanner) {
        System.out.println("\n--- [Generate Single Award Letter] ---");
        System.out.print("Enter Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete a = dao.getAthleteByRegistrationId(regId);
        if (a == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        Scholarship s = dao.getScholarshipByAthleteId(a.getAthleteId());
        if (s == null) {
            System.out.println("Athlete has not been evaluated yet. Evaluating now...");
            s = engine.evaluateSingleAthlete(a.getAthleteId());
        }

        try {
            String path = exporter.generateAwardLetter(a, s, REPORTS_DIR);
            System.out.println("[SUCCESS] Award letter written to: " + path);
        } catch (Exception e) {
            System.err.println("Failed to generate award letter: " + e.getMessage());
        }
    }

    private static void generateAllAwardLetters() {
        System.out.println("\n--- [Generate All Award Letters & Summary] ---");
        List<Athlete> athletes = dao.getAllAthletes();
        List<Scholarship> scholarships = dao.getAllScholarships();

        if (scholarships.isEmpty()) {
            System.out.println("No scholarships evaluated yet. Running evaluation now...");
            scholarships = engine.evaluateAllAthletes();
        }

        try {
            int count = exporter.generateAllAwardLetters(athletes, scholarships, REPORTS_DIR);
            String summaryPath = exporter.generateEvaluationSummary(scholarships, REPORTS_DIR);
            System.out.println("[SUCCESS] Generated " + count + " award letters in '" + REPORTS_DIR + "/'");
            System.out.println("[SUCCESS] Evaluation summary saved at: " + summaryPath);
        } catch (Exception e) {
            System.err.println("Error generating reports: " + e.getMessage());
        }
    }

    private static void viewScholarshipSummary() {
        System.out.println("\n--- [Scholarship Allocation Summary] ---");
        Map<String, Integer> summary = dao.getScholarshipSummary();
        double totalWaiver = dao.getTotalFeeWaiver();

        System.out.println("Gold Scholarships   (100% Fee Waiver) : " + summary.getOrDefault("GOLD", 0));
        System.out.println("Silver Scholarships (50% Fee Waiver)  : " + summary.getOrDefault("SILVER", 0));
        System.out.println("Bronze Scholarships (25% Fee Waiver)  : " + summary.getOrDefault("BRONZE", 0));
        System.out.println("No Award / Ineligible                 : " + summary.getOrDefault("NO_AWARD", 0));
        System.out.printf("Total Fee Waiver Amount Allocated     : %.2f%%\n", totalWaiver);
    }

    private static void searchAthleteByRegId(Scanner scanner) {
        System.out.print("\nEnter Registration ID to search: ");
        String regId = scanner.nextLine().trim();

        Athlete a = dao.getAthleteByRegistrationId(regId);
        if (a == null) {
            System.out.println("No athlete found with Registration ID: " + regId);
            return;
        }

        System.out.println("\n========================================================");
        System.out.println("ATHLETE PROFILE: " + a.getRegistrationId());
        System.out.println("========================================================");
        System.out.println("Name           : " + a.getStudentName());
        System.out.println("Department     : " + a.getDepartment());
        System.out.println("Year & Semester: Year " + a.getAcademicYear() + ", Sem " + a.getSemester());
        System.out.println("Sports Category: " + a.getSportsCategory());
        System.out.println("Sport Name     : " + a.getSportsName());
        System.out.println("Academic SGPA  : " + a.getSgpa() + " (" + a.getEligibilityStatus() + ")");

        FitnessLog f = dao.getFitnessLogByAthleteId(a.getAthleteId());
        if (f != null) {
            System.out.printf("Fitness Score  : %.2f | Endurance: %.2f\n", f.getFitnessScore(), f.getEnduranceScore());
        }

        TournamentScore t = dao.getTournamentScoreByAthleteId(a.getAthleteId());
        if (t != null) {
            System.out.printf("Tournament     : %s (%s) | Points: %.2f | Teamwork: %.2f\n",
                    t.getTournamentName(), t.getTournamentLevel(), t.getTournamentPoints(), t.getTeamworkScore());
        }

        TrialLog tr = dao.getTrialLogByAthleteId(a.getAthleteId());
        if (tr != null) {
            System.out.printf("Trial Log      : %s | Timing: %s | Score: %.2f | Discipline: %.2f\n",
                    tr.getEventName(), tr.getTrialTiming(), tr.getNormalizedTrialScore(), tr.getDisciplineScore());
        }

        Scholarship s = dao.getScholarshipByAthleteId(a.getAthleteId());
        if (s != null) {
            System.out.printf("Scholarship    : %s (Fee Waiver: %.2f%%) | SPI: %.2f%%\n",
                    s.getScholarshipType(), s.getFeeWaiver(), s.getSpi());
            System.out.println("Remarks        : " + s.getRemarks());
        } else {
            System.out.println("Scholarship    : Not evaluated yet.");
        }
        System.out.println("========================================================");
    }

    private static void testDatabaseConnection() {
        System.out.println("\nTesting database connection to: " + DBConnection.getInstance().getDbUrl());
        boolean ok = DBConnection.getInstance().testConnection();
        if (ok) {
            System.out.println("[SUCCESS] Connected to MySQL database successfully.");
        } else {
            System.err.println("[FAILED] Could not connect to MySQL database.");
        }
    }
}
