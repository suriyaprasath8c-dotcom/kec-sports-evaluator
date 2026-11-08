package com.kec.sports.evaluation;

import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Callable;

/*
 * EvaluationTask.java
 * Callable task executed concurrently by worker threads in the EvaluationEngine.
 * Computes the Sports Performance Index (SPI), checks category minimum thresholds,
 * verifies academic eligibility (SGPA >= 6.0), and assigns scholarship fee waivers.
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

        System.out.printf("[%s] Evaluating %s (%s)...%n", threadName, athlete.getStudentName(), category);

        // Academic filter: KEC / Anna University rule requires minimum 6.0 SGPA
        double sgpa = athlete.getSgpa();
        boolean isEligible = (sgpa >= 6.0);

        String currentStatus = isEligible ? "ELIGIBLE" : "INELIGIBLE";
        if (!currentStatus.equals(athlete.getEligibilityStatus())) {
            athlete.setEligibilityStatus(currentStatus);
            if (dao != null) {
                dao.updateEligibilityStatus(athlete.getAthleteId(), currentStatus);
            }
        }

        // Pull component scores from respective phase logs
        double fitness = (fitnessLog != null) ? fitnessLog.getFitnessScore() : 0.0;
        double tournament = (tournamentScore != null) ? tournamentScore.getTournamentPoints() : 0.0;
        double teamwork = (tournamentScore != null) ? tournamentScore.getTeamworkScore() : 0.0;
        double trialScore = (trialLog != null) ? trialLog.getNormalizedTrialScore() : 0.0;
        double discipline = (trialLog != null && trialLog.getDisciplineScore() > 0)
                ? trialLog.getDisciplineScore() : 85.0; // Institutional default rating

        double rawSpi = 0.0;
        boolean thresholdPassed = true;
        String thresholdNote = "";

        // Category-wise formula calculations and minimum thresholds
        if ("INDIVIDUAL_SPORTS".equalsIgnoreCase(category)) {
            // Formula: 30% Fitness + 50% Tournaments + 20% Discipline
            // Threshold: Tournament minimum 40%
            rawSpi = (fitness * 0.30) + (tournament * 0.50) + (discipline * 0.20);
            if (tournament < 40.0) {
                thresholdPassed = false;
                thresholdNote = "Tournament score (" + tournament + ") is below the 40% minimum threshold.";
            }
        } else if ("TEAM_SPORTS".equalsIgnoreCase(category)) {
            // Formula: 40% Fitness + 40% Tournaments + 20% Teamwork
            // Threshold: Fitness minimum 50%
            rawSpi = (fitness * 0.40) + (tournament * 0.40) + (teamwork * 0.20);
            if (fitness < 50.0) {
                thresholdPassed = false;
                thresholdNote = "Fitness score (" + fitness + ") is below the 50% minimum threshold.";
            }
        } else if ("TRACK_AND_FIELD".equalsIgnoreCase(category)) {
            // Formula: 50% Fitness + 40% Timed Trials + 10% Discipline
            // Threshold: Trial minimum 50%
            rawSpi = (fitness * 0.50) + (trialScore * 0.40) + (discipline * 0.10);
            if (trialScore < 50.0) {
                thresholdPassed = false;
                thresholdNote = "Trial score (" + trialScore + ") is below the 50% minimum threshold.";
            }
        } else {
            rawSpi = (fitness * 0.40) + (tournament * 0.40) + (discipline * 0.20);
        }

        // Round SPI to 2 decimal places
        BigDecimal bd = BigDecimal.valueOf(rawSpi).setScale(2, RoundingMode.HALF_UP);
        double finalSpi = bd.doubleValue();

        // Determine scholarship tier and fee waiver
        String awardTier = "NO_AWARD";
        double feeWaiver = 0.0;
        String evalStatus = "EVALUATED";
        String remarks;

        if (!isEligible) {
            awardTier = "NO_AWARD";
            feeWaiver = 0.0;
            evalStatus = "INELIGIBLE";
            remarks = "INELIGIBLE: Academic SGPA " + sgpa + " is below 6.0 minimum requirement. Scholarship revoked.";
        } else if (!thresholdPassed) {
            awardTier = "NO_AWARD";
            feeWaiver = 0.0;
            remarks = "NO AWARD: Did not meet category threshold. " + thresholdNote;
        } else {
            // Check scholarship tiers based on institutional policy
            if (finalSpi >= 90.0) {
                awardTier = "GOLD";
                feeWaiver = 100.0;
                remarks = "GOLD SCHOLARSHIP: Outstanding athletic performance (" + finalSpi + "%). 100% Fee Waiver.";
            } else if (finalSpi >= 75.0) {
                awardTier = "SILVER";
                feeWaiver = 50.0;
                remarks = "SILVER SCHOLARSHIP: Excellent performance (" + finalSpi + "%). 50% Fee Waiver.";
            } else if (finalSpi >= 60.0) {
                awardTier = "BRONZE";
                feeWaiver = 25.0;
                remarks = "BRONZE SCHOLARSHIP: Commendable performance (" + finalSpi + "%). 25% Fee Waiver.";
            } else {
                awardTier = "NO_AWARD";
                feeWaiver = 0.0;
                remarks = "NO AWARD: SPI score (" + finalSpi + "%) is below the 60% requirement.";
            }
        }

        Scholarship scholarship = new Scholarship(
                athlete.getAthleteId(), fitness, tournament, trialScore,
                discipline, teamwork, finalSpi, awardTier,
                feeWaiver, evalStatus, remarks
        );

        scholarship.setRegistrationId(regId);
        scholarship.setStudentName(athlete.getStudentName());
        scholarship.setDepartment(athlete.getDepartment());
        scholarship.setSportsCategory(category);
        scholarship.setSportsName(athlete.getSportsName());
        scholarship.setSgpa(sgpa);

        // Update database record
        if (dao != null) {
            dao.insertOrUpdateScholarship(scholarship);
        }

        System.out.printf("[%s] Done %s -> SPI: %.2f%% | Award: %s (%.0f%% waiver)%n",
                threadName, regId, finalSpi, awardTier, feeWaiver);

        return scholarship;
    }
}
