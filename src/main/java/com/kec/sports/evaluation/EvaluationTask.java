package com.kec.sports.evaluation;

import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Callable;

/**
 * Callable task for evaluating an athlete's athletic performance and scholarship tier.
 * Runs in parallel within a thread pool and displays thread execution logs.
 */
public class EvaluationTask implements Callable<Scholarship> {

    private final Athlete athlete;
    private final FitnessLog fitnessLog;
    private final TournamentScore tournamentScore;
    private final TrialLog trialLog;
    private final SportsDAO dao;

    public EvaluationTask(Athlete athlete, FitnessLog fitnessLog,
                          TournamentScore tournamentScore, TrialLog trialLog,
                          SportsDAO dao) {
        this.athlete = athlete;
        this.fitnessLog = fitnessLog;
        this.tournamentScore = tournamentScore;
        this.trialLog = trialLog;
        this.dao = dao;
    }

    @Override
    public Scholarship call() throws Exception {
        String threadName = Thread.currentThread().getName();
        String regId = athlete.getRegistrationId();
        String category = athlete.getSportsCategory();

        System.out.println("[" + threadName + "] Evaluating " + category + " for Athlete " + regId + " (" + athlete.getStudentName() + ")");

        // Step 1: Check Academic Eligibility (SGPA >= 6.0)
        double sgpa = athlete.getSgpa();
        boolean isAcademicallyEligible = (sgpa >= 6.0);

        // Update eligibility status in athlete table if changed
        String currentStatus = isAcademicallyEligible ? "ELIGIBLE" : "INELIGIBLE";
        if (!currentStatus.equals(athlete.getEligibilityStatus())) {
            athlete.setEligibilityStatus(currentStatus);
            if (dao != null) {
                dao.updateEligibilityStatus(athlete.getAthleteId(), currentStatus);
            }
        }

        // Component scores
        double fitScore = (fitnessLog != null) ? fitnessLog.getFitnessScore() : 0.0;
        double tournScore = (tournamentScore != null) ? tournamentScore.getTournamentPoints() : 0.0;
        double teamScore = (tournamentScore != null) ? tournamentScore.getTeamworkScore() : 0.0;
        double trScore = (trialLog != null) ? trialLog.getNormalizedTrialScore() : 0.0;
        double discScore = (trialLog != null && trialLog.getDisciplineScore() > 0)
                ? trialLog.getDisciplineScore() : 85.0; // Institutional baseline discipline score

        double rawSpi = 0.0;
        boolean thresholdMet = true;
        String thresholdFailureReason = "";

        // Step 2: Category-Specific SPI Calculation & Minimum Thresholds
        if ("INDIVIDUAL_SPORTS".equalsIgnoreCase(category)) {
            // SPI = (Fitness × 0.30) + (Tournaments × 0.50) + (Discipline × 0.20)
            // Tournament Minimum Threshold = 40%
            rawSpi = (fitScore * 0.30) + (tournScore * 0.50) + (discScore * 0.20);
            if (tournScore < 40.0) {
                thresholdMet = false;
                thresholdFailureReason = "Tournament score (" + tournScore + ") below minimum threshold of 40%.";
            }
        } else if ("TEAM_SPORTS".equalsIgnoreCase(category)) {
            // SPI = (Fitness × 0.40) + (Tournaments × 0.40) + (Teamwork Score × 0.20)
            // Fitness Minimum Threshold = 50%
            rawSpi = (fitScore * 0.40) + (tournScore * 0.40) + (teamScore * 0.20);
            if (fitScore < 50.0) {
                thresholdMet = false;
                thresholdFailureReason = "Fitness score (" + fitScore + ") below minimum threshold of 50%.";
            }
        } else if ("TRACK_AND_FIELD".equalsIgnoreCase(category)) {
            // SPI = (Fitness × 0.50) + (Timed Trials × 0.40) + (Discipline × 0.10)
            // Trial Minimum Threshold = 50%
            rawSpi = (fitScore * 0.50) + (trScore * 0.40) + (discScore * 0.10);
            if (trScore < 50.0) {
                thresholdMet = false;
                thresholdFailureReason = "Trial score (" + trScore + ") below minimum threshold of 50%.";
            }
        } else {
            // Default weighted average
            rawSpi = (fitScore * 0.40) + (tournScore * 0.40) + (discScore * 0.20);
        }

        // Round SPI to 2 decimal places using BigDecimal
        BigDecimal bdSpi = BigDecimal.valueOf(rawSpi).setScale(2, RoundingMode.HALF_UP);
        double finalSpi = bdSpi.doubleValue();

        // Step 3: Determine Scholarship Tier & Fee Waiver
        String scholarshipType = "NO_AWARD";
        double feeWaiver = 0.0;
        String evaluationStatus = "EVALUATED";
        String remarks;

        if (!isAcademicallyEligible) {
            scholarshipType = "NO_AWARD";
            feeWaiver = 0.0;
            evaluationStatus = "INELIGIBLE";
            remarks = "INELIGIBLE: Academic SGPA is " + sgpa + " (< 6.0 minimum required). Scholarship revoked.";
        } else if (!thresholdMet) {
            scholarshipType = "NO_AWARD";
            feeWaiver = 0.0;
            remarks = "NO AWARD: Category minimum threshold not met. " + thresholdFailureReason;
        } else {
            // Threshold met & academically eligible
            if (finalSpi >= 90.0) {
                scholarshipType = "GOLD";
                feeWaiver = 100.0;
                remarks = "GOLD SCHOLARSHIP: Outstanding sports performance (" + finalSpi + "%). 100% Fee Waiver awarded.";
            } else if (finalSpi >= 75.0) {
                scholarshipType = "SILVER";
                feeWaiver = 50.0;
                remarks = "SILVER SCHOLARSHIP: Excellent sports performance (" + finalSpi + "%). 50% Fee Waiver awarded.";
            } else if (finalSpi >= 60.0) {
                scholarshipType = "BRONZE";
                feeWaiver = 25.0;
                remarks = "BRONZE SCHOLARSHIP: Commendable sports performance (" + finalSpi + "%). 25% Fee Waiver awarded.";
            } else {
                scholarshipType = "NO_AWARD";
                feeWaiver = 0.0;
                remarks = "NO AWARD: SPI (" + finalSpi + "%) is below the minimum 60% requirement for scholarships.";
            }
        }

        Scholarship scholarship = new Scholarship(
                athlete.getAthleteId(), fitScore, tournScore, trScore,
                discScore, teamScore, finalSpi, scholarshipType,
                feeWaiver, evaluationStatus, remarks
        );

        // Populate joined metadata
        scholarship.setRegistrationId(regId);
        scholarship.setStudentName(athlete.getStudentName());
        scholarship.setDepartment(athlete.getDepartment());
        scholarship.setSportsCategory(category);
        scholarship.setSportsName(athlete.getSportsName());
        scholarship.setSgpa(sgpa);

        // Save to Database
        if (dao != null) {
            dao.insertOrUpdateScholarship(scholarship);
        }

        System.out.println("[" + threadName + "] COMPLETED " + regId + " -> SPI: " + finalSpi + "% | Award: " + scholarshipType + " | Waiver: " + feeWaiver + "%");

        return scholarship;
    }
}
