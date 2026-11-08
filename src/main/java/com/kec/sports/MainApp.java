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

/*
 * MainApp.java
 * Command-line interface for testing and demonstrating all modules of the
 * KEC Student Sports Athletic Performance & Scholarship Evaluator.
 *
 * Team Project - Kongu Engineering College (Autonomous)
 * Student 1: Vaisnav K (App Host & Multithreading)
 * Student 2: Database Admin (OCI VM 2 MySQL Host)
 */
public class MainApp {

    private static final String REPORTS_DIR = "reports";
    private static final SportsDAO dao = new SportsDAO();
    private static final EvaluationEngine engine = new EvaluationEngine(dao);
    private static final ReportExporter exporter = new ReportExporter();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("================================================================");
        System.out.println("   KONGU ENGINEERING COLLEGE (AUTONOMOUS) - PERUNDURAI");
        System.out.println("       Sports Board • Scholarship Evaluation System");
        System.out.println("================================================================");

        // Ping database before presenting menu
        boolean dbOk = DBConnection.getInstance().testConnection();
        if (dbOk) {
            System.out.println("Database status: Connected to MySQL (" + DBConnection.getInstance().getDbUrl() + ")");
        } else {
            System.err.println("Notice: Could not connect to MySQL at startup.");
            System.err.println("Please check application.properties or run OCI VM 2 MySQL database.");
        }

        boolean keepRunning = true;
        while (keepRunning) {
            printMenu();
            System.out.print("Select an option (1-15): ");
            String input = scanner.nextLine().trim();

            try {
                int option = Integer.parseInt(input);
                switch (option) {
                    case 1:
                        registerAthlete(scanner);
                        break;
                    case 2:
                        recordFitness(scanner);
                        break;
                    case 3:
                        recordTournament(scanner);
                        break;
                    case 4:
                        recordTrials(scanner);
                        break;
                    case 5:
                        importCsvTimings(scanner);
                        break;
                    case 6:
                        listAllAthletes();
                        break;
                    case 7:
                        verifyAcademicEligibility(scanner);
                        break;
                    case 8:
                        evaluateSingle(scanner);
                        break;
                    case 9:
                        evaluateAllParallel();
                        break;
                    case 10:
                        generateSingleLetter(scanner);
                        break;
                    case 11:
                        generateAllLetters();
                        break;
                    case 12:
                        viewScholarshipReport();
                        break;
                    case 13:
                        searchAthlete(scanner);
                        break;
                    case 14:
                        testDbConnection();
                        break;
                    case 15:
                        System.out.println("\nExiting application. Thank you!");
                        keepRunning = false;
                        break;
                    default:
                        System.out.println("Please select a number between 1 and 15.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            if (keepRunning) {
                System.out.println("\nPress [Enter] to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------- MAIN APPLICATION MENU -----------------");
        System.out.println(" 1. Register Student Athlete (Phase 1)");
        System.out.println(" 2. Record Physical Fitness & Endurance Scores (Phase 1)");
        System.out.println(" 3. Record Tournament Performance (Phase 2)");
        System.out.println(" 4. Record Track & Field Trials & Discipline (Phase 3)");
        System.out.println(" 5. Import Trial Timings from CSV File");
        System.out.println(" 6. View All Registered Athletes");
        System.out.println(" 7. Verify Academic Eligibility (SGPA Filter >= 6.0)");
        System.out.println(" 8. Evaluate Single Athlete (SPI Calculation)");
        System.out.println(" 9. Evaluate All Athletes (Multithreaded Concurrent Execution)");
        System.out.println(" 10. Generate Scholarship Award Letter (.txt)");
        System.out.println(" 11. Generate All Award Letters & Summary Report");
        System.out.println(" 12. View Scholarship Summary & Total Fee Waiver");
        System.out.println(" 13. Search Athlete by Registration ID");
        System.out.println(" 14. Test MySQL Database Connection");
        System.out.println(" 15. Exit");
        System.out.println("---------------------------------------------------------");
    }

    private static void registerAthlete(Scanner scanner) {
        System.out.println("\n--- [Phase 1: Athlete Registration] ---");
        System.out.print("Student Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Department (e.g. CSE, ECE, MECH, EEE, IT, CIVIL): ");
        String dept = scanner.nextLine().trim().toUpperCase();

        System.out.print("Academic Year (1-4): ");
        int year = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Semester (1-8): ");
        int sem = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Sports Category:");
        System.out.println("  1. Individual Sports (Badminton, Table Tennis, Tennis)");
        System.out.println("  2. Team Sports (Cricket, Volleyball, Football, Basketball)");
        System.out.println("  3. Track and Field (100m, 400m Relay, Long Jump, etc.)");
        System.out.print("Choice (1-3): ");
        int catChoice = Integer.parseInt(scanner.nextLine().trim());
        String category = "INDIVIDUAL_SPORTS";
        if (catChoice == 2) category = "TEAM_SPORTS";
        else if (catChoice == 3) category = "TRACK_AND_FIELD";

        System.out.print("Sport / Discipline Name: ");
        String sportName = scanner.nextLine().trim();

        System.out.print("Academic SGPA (0.00 - 10.00): ");
        double sgpa = Double.parseDouble(scanner.nextLine().trim());

        int currentYear = 2026;
        int nextSeq = dao.getNextSequenceNumber(currentYear);
        String regId = StringProcessor.generateRegistrationId(nextSeq, currentYear);
        String status = (sgpa >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";

        Athlete athlete = new Athlete(regId, name, dept, year, sem, category, sportName, sgpa, status);
        int athleteId = dao.insertAthlete(athlete);

        if (athleteId > 0) {
            System.out.println("Successfully registered athlete!");
            System.out.println("  Assigned Reg ID : " + regId);
            System.out.println("  Eligibility     : " + status + (sgpa < 6.0 ? " (SGPA < 6.0)" : ""));

            System.out.print("Enter fitness scores now? (y/n): ");
            String ans = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(ans)) {
                System.out.print("Fitness Score (0-100): ");
                double fit = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Endurance Score (0-100): ");
                double end = Double.parseDouble(scanner.nextLine().trim());
                FitnessLog log = new FitnessLog(athleteId, fit, end, new Date(System.currentTimeMillis()));
                dao.insertFitnessLog(log);
                System.out.println("Fitness scores recorded.");
            }
        } else {
            System.err.println("Registration failed. Please check database logs.");
        }
    }

    private static void recordFitness(Scanner scanner) {
        System.out.println("\n--- [Phase 1: Record Fitness Scores] ---");
        System.out.print("Enter Registration ID (e.g. KEC-SPT-2026-001): ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found for ID: " + regId);
            return;
        }

        System.out.println("Athlete: " + athlete.getStudentName() + " (" + athlete.getSportsName() + ")");
        System.out.print("Fitness Score (0-100): ");
        double fit = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Endurance Score (0-100): ");
        double end = Double.parseDouble(scanner.nextLine().trim());

        FitnessLog log = new FitnessLog(athlete.getAthleteId(), fit, end, new Date(System.currentTimeMillis()));
        int id = dao.insertFitnessLog(log);
        if (id > 0) {
            System.out.println("Fitness record saved successfully (Record ID: " + id + ").");
        } else {
            System.err.println("Failed to save fitness record.");
        }
    }

    private static void recordTournament(Scanner scanner) {
        System.out.println("\n--- [Phase 2: Record Tournament Performance] ---");
        System.out.print("Enter Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        System.out.println("Athlete: " + athlete.getStudentName() + " | Category: " + athlete.getSportsCategory());
        System.out.print("Tournament Name: ");
        String name = scanner.nextLine().trim();

        System.out.println("Tournament Level:");
        System.out.println("  1. ZONAL\n  2. INTER_COLLEGE\n  3. STATE\n  4. NATIONAL");
        System.out.print("Choice (1-4): ");
        int lvlChoice = Integer.parseInt(scanner.nextLine().trim());
        String level = "ZONAL";
        if (lvlChoice == 2) level = "INTER_COLLEGE";
        else if (lvlChoice == 3) level = "STATE";
        else if (lvlChoice == 4) level = "NATIONAL";

        System.out.print("Tournament Points (0-100): ");
        double points = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Achievement (e.g. Gold Medalist, Semi-Finalist): ");
        String achievement = scanner.nextLine().trim();

        double teamwork = 0.0;
        if ("TEAM_SPORTS".equalsIgnoreCase(athlete.getSportsCategory())) {
            System.out.print("Teamwork Score (0-100): ");
            teamwork = Double.parseDouble(scanner.nextLine().trim());
        }

        TournamentScore score = new TournamentScore(athlete.getAthleteId(), name, level, points, achievement, teamwork);
        int id = dao.insertTournamentScore(score);
        if (id > 0) {
            System.out.println("Tournament record saved successfully.");
        } else {
            System.err.println("Could not save tournament record.");
        }
    }

    private static void recordTrials(Scanner scanner) {
        System.out.println("\n--- [Phase 3: Record Timed Trials & Discipline] ---");
        System.out.print("Enter Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        System.out.print("Event Name (e.g. 100m Sprint, 400m Relay): ");
        String event = scanner.nextLine().trim();

        System.out.print("Trial Timing (Format MM:SS.ms, e.g. 00:11.25 or 02:15.45): ");
        String timing = scanner.nextLine().trim();

        if (!StringProcessor.validateTrialTiming(timing)) {
            System.err.println("Invalid format. Please use MM:SS.ms (e.g. 00:11.25)");
            return;
        }

        double actualSeconds = StringProcessor.parseTrialTiming(timing);
        double benchmarkSeconds = StringProcessor.getBenchmarkTime(event);
        double normalizedScore = StringProcessor.normalizeTrialScore(actualSeconds, benchmarkSeconds);

        System.out.printf("  Actual Time: %.2fs | Benchmark: %.2fs | Converted Score: %.2f/100%n",
                actualSeconds, benchmarkSeconds, normalizedScore);

        System.out.print("Discipline Score (0-100): ");
        double discipline = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Coach Remarks: ");
        String remarks = StringProcessor.sanitizeCoachRemarks(scanner.nextLine().trim());

        TrialLog log = new TrialLog(athlete.getAthleteId(), event, timing, normalizedScore, discipline, remarks);
        int id = dao.insertTrialLog(log);
        if (id > 0) {
            System.out.println("Trial timing recorded successfully.");
        } else {
            System.err.println("Could not save trial log.");
        }
    }

    private static void importCsvTimings(Scanner scanner) {
        System.out.println("\n--- [CSV Batch Import: Track & Field Trials] ---");
        System.out.print("Enter CSV path (press Enter for 'uploads/trial_timings.csv'): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "uploads/trial_timings.csv";
        }

        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File not found at: " + file.getAbsolutePath());
            return;
        }

        List<TrialLog> imported = CSVHandler.importTrialTimings(path);
        int count = 0;
        for (TrialLog log : imported) {
            if (log.getRegistrationId() != null) {
                Athlete athlete = dao.getAthleteByRegistrationId(log.getRegistrationId());
                if (athlete != null) {
                    log.setAthleteId(athlete.getAthleteId());
                    double actual = StringProcessor.parseTrialTiming(log.getTrialTiming());
                    double bench = StringProcessor.getBenchmarkTime(log.getEventName());
                    log.setNormalizedTrialScore(StringProcessor.normalizeTrialScore(actual, bench));
                    dao.insertTrialLog(log);
                    count++;
                }
            }
        }
        System.out.println("Successfully imported and saved " + count + " trial records.");
    }

    private static void listAllAthletes() {
        System.out.println("\n--- [Registered Student Athletes] ---");
        List<Athlete> list = dao.getAllAthletes();
        if (list.isEmpty()) {
            System.out.println("No athletes found.");
            return;
        }

        System.out.printf("%-4s %-18s %-20s %-6s %-18s %-15s %-5s %-10s%n",
                "ID", "REG ID", "NAME", "DEPT", "CATEGORY", "SPORT", "SGPA", "STATUS");
        System.out.println("--------------------------------------------------------------------------------------------------");
        for (Athlete a : list) {
            System.out.printf("%-4d %-18s %-20s %-6s %-18s %-15s %5.2f %-10s%n",
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

    private static void verifyAcademicEligibility(Scanner scanner) {
        System.out.println("\n--- [Academic Eligibility Verification] ---");
        System.out.print("Enter Registration ID (leave blank to verify all): ");
        String regId = scanner.nextLine().trim();

        if (!regId.isEmpty()) {
            Athlete a = dao.getAthleteByRegistrationId(regId);
            if (a == null) {
                System.err.println("Athlete not found: " + regId);
                return;
            }
            String status = (a.getSgpa() >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";
            dao.updateEligibilityStatus(a.getAthleteId(), status);
            System.out.printf("Athlete: %s | SGPA: %.2f | Status: %s%n", a.getStudentName(), a.getSgpa(), status);
            if ("INELIGIBLE".equals(status)) {
                System.out.println("Reason: SGPA < 6.0 violates institutional minimum academic criteria.");
            }
        } else {
            List<Athlete> all = dao.getAllAthletes();
            int el = 0, inel = 0;
            for (Athlete a : all) {
                String st = (a.getSgpa() >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";
                dao.updateEligibilityStatus(a.getAthleteId(), st);
                if ("ELIGIBLE".equals(st)) el++;
                else inel++;
            }
            System.out.printf("Checked %d athletes. Eligible: %d | Ineligible: %d%n", all.size(), el, inel);
        }
    }

    private static void evaluateSingle(Scanner scanner) {
        System.out.print("\nEnter Registration ID to evaluate: ");
        String regId = scanner.nextLine().trim();

        Athlete athlete = dao.getAthleteByRegistrationId(regId);
        if (athlete == null) {
            System.err.println("Athlete not found: " + regId);
            return;
        }

        Scholarship s = engine.evaluateSingleAthlete(athlete.getAthleteId());
        if (s != null) {
            System.out.println("\nEvaluation Result:");
            System.out.println("  Athlete          : " + athlete.getStudentName());
            System.out.println("  Category         : " + athlete.getSportsCategory());
            System.out.println("  SGPA             : " + athlete.getSgpa() + " (" + athlete.getEligibilityStatus() + ")");
            System.out.println("  Computed SPI     : " + s.getSpi() + "%");
            System.out.println("  Award Tier       : " + s.getScholarshipType());
            System.out.println("  Fee Waiver       : " + s.getFeeWaiver() + "%");
            System.out.println("  Remarks          : " + s.getRemarks());
        }
    }

    private static void evaluateAllParallel() {
        List<Scholarship> results = engine.evaluateAllAthletes();
        System.out.printf("Multithreaded evaluation completed. Total evaluated: %d%n", results.size());
    }

    private static void generateSingleLetter(Scanner scanner) {
        System.out.print("\nEnter Registration ID: ");
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
            System.out.println("Award letter generated: " + path);
        } catch (Exception e) {
            System.err.println("Could not generate letter: " + e.getMessage());
        }
    }

    private static void generateAllLetters() {
        List<Athlete> athletes = dao.getAllAthletes();
        List<Scholarship> scholarships = dao.getAllScholarships();

        if (scholarships.isEmpty()) {
            System.out.println("Running evaluation first...");
            scholarships = engine.evaluateAllAthletes();
        }

        try {
            int count = exporter.generateAllAwardLetters(athletes, scholarships, REPORTS_DIR);
            String summary = exporter.generateEvaluationSummary(scholarships, REPORTS_DIR);
            System.out.println("Generated " + count + " letters in '" + REPORTS_DIR + "/'");
            System.out.println("Summary file saved at: " + summary);
        } catch (Exception e) {
            System.err.println("Error generating reports: " + e.getMessage());
        }
    }

    private static void viewScholarshipReport() {
        System.out.println("\n--- [Scholarship Summary & Allocations] ---");
        Map<String, Integer> summary = dao.getScholarshipSummary();
        double totalWaiver = dao.getTotalFeeWaiver();

        System.out.println("  Gold Scholarships   (100% Waiver) : " + summary.getOrDefault("GOLD", 0));
        System.out.println("  Silver Scholarships (50% Waiver)  : " + summary.getOrDefault("SILVER", 0));
        System.out.println("  Bronze Scholarships (25% Waiver)  : " + summary.getOrDefault("BRONZE", 0));
        System.out.println("  No Award / Ineligible             : " + summary.getOrDefault("NO_AWARD", 0));
        System.out.printf("  Total Fee Waiver Percentage Allocated: %.2f%%%n", totalWaiver);
    }

    private static void searchAthlete(Scanner scanner) {
        System.out.print("\nEnter Registration ID: ");
        String regId = scanner.nextLine().trim();

        Athlete a = dao.getAthleteByRegistrationId(regId);
        if (a == null) {
            System.out.println("No athlete found for ID: " + regId);
            return;
        }

        System.out.println("\n--- Athlete Profile ---");
        System.out.println("Registration ID : " + a.getRegistrationId());
        System.out.println("Name            : " + a.getStudentName());
        System.out.println("Department      : " + a.getDepartment() + " (Year " + a.getAcademicYear() + ", Sem " + a.getSemester() + ")");
        System.out.println("Category        : " + a.getSportsCategory());
        System.out.println("Sport Name      : " + a.getSportsName());
        System.out.printf("SGPA            : %.2f (%s)%n", a.getSgpa(), a.getEligibilityStatus());

        Scholarship s = dao.getScholarshipByAthleteId(a.getAthleteId());
        if (s != null) {
            System.out.printf("SPI Score       : %.2f%%%n", s.getSpi());
            System.out.println("Award Tier      : " + s.getScholarshipType() + " (" + s.getFeeWaiver() + "% fee waiver)");
            System.out.println("Evaluation Note : " + s.getRemarks());
        } else {
            System.out.println("Scholarship     : Not evaluated yet.");
        }
    }

    private static void testDbConnection() {
        System.out.println("Testing connection to: " + DBConnection.getInstance().getDbUrl());
        boolean ok = DBConnection.getInstance().testConnection();
        System.out.println("Result: " + (ok ? "Connection successful!" : "Connection failed. Check credentials."));
    }
}
