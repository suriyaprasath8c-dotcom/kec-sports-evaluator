package com.kec.sports.evaluation;

import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multithreaded evaluation coordinator using Java ExecutorService.
 * Executes EvaluationTasks concurrently across multiple athlete records and sports disciplines.
 */
public class EvaluationEngine {

    private final SportsDAO dao;
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    public EvaluationEngine(SportsDAO dao) {
        this.dao = dao;
    }

    /**
     * Evaluates all athletes registered in the system using parallel threads.
     * Displays thread names and execution details in stdout.
     */
    public List<Scholarship> evaluateAllAthletes() {
        List<Athlete> athletes = dao.getAllAthletes();
        System.out.println("\n========================================================");
        System.out.println(">>> STARTING MULTITHREADED EVALUATION ENGINE");
        System.out.println(">>> Total Athletes to Evaluate: " + athletes.size());
        System.out.println("========================================================");

        List<Scholarship> results = new ArrayList<>();
        if (athletes.isEmpty()) {
            System.out.println("[EvaluationEngine] No athletes found in the database.");
            return results;
        }

        // Dedicated fixed thread pool for concurrent category evaluations
        ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "EvaluationThread-" + (counter++));
            }
        });

        List<Future<Scholarship>> futures = new ArrayList<>();

        for (Athlete athlete : athletes) {
            FitnessLog fitnessLog = dao.getFitnessLogByAthleteId(athlete.getAthleteId());
            TournamentScore tournamentScore = dao.getTournamentScoreByAthleteId(athlete.getAthleteId());
            TrialLog trialLog = dao.getTrialLogByAthleteId(athlete.getAthleteId());

            EvaluationTask task = new EvaluationTask(athlete, fitnessLog, tournamentScore, trialLog, dao);
            futures.add(executor.submit(task));
        }

        // Collect all results safely
        for (int i = 0; i < futures.size(); i++) {
            Future<Scholarship> future = futures.get(i);
            try {
                Scholarship scholarship = future.get(10, TimeUnit.SECONDS);
                if (scholarship != null) {
                    results.add(scholarship);
                }
            } catch (InterruptedException e) {
                System.err.println("[EvaluationEngine] Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                System.err.println("[EvaluationEngine] Error evaluating athlete task: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                System.err.println("[EvaluationEngine] Task timed out for index " + i);
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("========================================================");
        System.out.println(">>> EVALUATION FINISHED: Processed " + results.size() + " athletes.");
        System.out.println("========================================================\n");

        return results;
    }

    /**
     * Evaluates a single athlete by athlete ID.
     */
    public Scholarship evaluateSingleAthlete(int athleteId) {
        Athlete athlete = dao.getAthleteById(athleteId);
        if (athlete == null) {
            System.err.println("[EvaluationEngine] Athlete not found with ID: " + athleteId);
            return null;
        }
        FitnessLog fitnessLog = dao.getFitnessLogByAthleteId(athleteId);
        TournamentScore tournamentScore = dao.getTournamentScoreByAthleteId(athleteId);
        TrialLog trialLog = dao.getTrialLogByAthleteId(athleteId);

        EvaluationTask task = new EvaluationTask(athlete, fitnessLog, tournamentScore, trialLog, dao);
        try {
            return task.call();
        } catch (Exception e) {
            System.err.println("[EvaluationEngine] Error evaluating single athlete: " + e.getMessage());
            return null;
        }
    }

    /**
     * Evaluates all athletes of a specific sports category concurrently.
     */
    public List<Scholarship> evaluateByCategory(String category) {
        List<Athlete> athletes = dao.getAthletesByCategory(category);
        List<Scholarship> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<Future<Scholarship>> futures = new ArrayList<>();

        for (Athlete athlete : athletes) {
            FitnessLog fitnessLog = dao.getFitnessLogByAthleteId(athlete.getAthleteId());
            TournamentScore tournamentScore = dao.getTournamentScoreByAthleteId(athlete.getAthleteId());
            TrialLog trialLog = dao.getTrialLogByAthleteId(athlete.getAthleteId());
            EvaluationTask task = new EvaluationTask(athlete, fitnessLog, tournamentScore, trialLog, dao);
            futures.add(executor.submit(task));
        }

        for (Future<Scholarship> f : futures) {
            try {
                results.add(f.get());
            } catch (Exception e) {
                System.err.println("[EvaluationEngine] Task error: " + e.getMessage());
            }
        }
        executor.shutdown();
        return results;
    }
}
