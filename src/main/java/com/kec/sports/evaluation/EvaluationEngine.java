package com.kec.sports.evaluation;

import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
 * EvaluationEngine.java
 * Coordinates parallel multithreaded evaluation using a fixed thread pool.
 * Dispatches EvaluationTask workers and logs thread activities to console.
 */
public class EvaluationEngine {

    private final SportsDAO dao;
    private static final int THREAD_POOL_SIZE = 4;

    public EvaluationEngine(SportsDAO dao) {
        this.dao = dao;
    }

    // Evaluates all athletes in parallel across sports categories
    public List<Scholarship> evaluateAllAthletes() {
        List<Athlete> athletes = dao.getAllAthletes();
        System.out.println("------------------------------------------------------------");
        System.out.println("Starting Parallel Evaluation Engine (Pool Size: " + THREAD_POOL_SIZE + ")");
        System.out.println("Athletes found for evaluation: " + athletes.size());
        System.out.println("------------------------------------------------------------");

        List<Scholarship> results = new ArrayList<>();
        if (athletes.isEmpty()) {
            System.out.println("No athletes found in database.");
            return results;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, new ThreadFactory() {
            private int threadNum = 1;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "EvaluationThread-" + (threadNum++));
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

        // Collect results safely
        for (int i = 0; i < futures.size(); i++) {
            try {
                Scholarship scholarship = futures.get(i).get(10, TimeUnit.SECONDS);
                if (scholarship != null) {
                    results.add(scholarship);
                }
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                System.err.println("Task execution failed for athlete: " + e.getCause().getMessage());
            } catch (TimeoutException e) {
                System.err.println("Task timed out for athlete index: " + i);
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

        System.out.println("------------------------------------------------------------");
        System.out.println("Evaluation finished: Successfully processed " + results.size() + " athletes.");
        System.out.println("------------------------------------------------------------");

        return results;
    }

    public Scholarship evaluateSingleAthlete(int athleteId) {
        Athlete athlete = dao.getAthleteById(athleteId);
        if (athlete == null) {
            System.err.println("Athlete not found with ID: " + athleteId);
            return null;
        }
        FitnessLog fitnessLog = dao.getFitnessLogByAthleteId(athleteId);
        TournamentScore tournamentScore = dao.getTournamentScoreByAthleteId(athleteId);
        TrialLog trialLog = dao.getTrialLogByAthleteId(athleteId);

        EvaluationTask task = new EvaluationTask(athlete, fitnessLog, tournamentScore, trialLog, dao);
        try {
            return task.call();
        } catch (Exception e) {
            System.err.println("Evaluation error for single athlete: " + e.getMessage());
            return null;
        }
    }
}
