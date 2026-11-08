package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.evaluation.EvaluationEngine;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.Scholarship;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/api/evaluate/*")
public class EvaluationServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private final SportsDAO dao = new SportsDAO();
    private final EvaluationEngine engine = new EvaluationEngine(dao);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.contains("summary")) {
            Map<String, Integer> summary = dao.getScholarshipSummary();
            double totalWaiver = dao.getTotalFeeWaiver();
            int totalAthletes = dao.getTotalAthleteCount();
            int eligible = dao.getEligibleCount();
            int ineligible = dao.getIneligibleCount();

            List<Scholarship> list = dao.getAllScholarships();
            double sumSpi = 0.0;
            for (Scholarship s : list) sumSpi += s.getSpi();
            double avgSpi = list.isEmpty() ? 0.0 : Math.round((sumSpi / list.size()) * 100.0) / 100.0;

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("totalAthletes", totalAthletes);
            res.addProperty("eligibleAthletes", eligible);
            res.addProperty("ineligibleAthletes", ineligible);
            res.addProperty("evaluatedCount", list.size());
            res.addProperty("goldCount", summary.getOrDefault("GOLD", 0));
            res.addProperty("silverCount", summary.getOrDefault("SILVER", 0));
            res.addProperty("bronzeCount", summary.getOrDefault("BRONZE", 0));
            res.addProperty("noAwardCount", summary.getOrDefault("NO_AWARD", 0));
            res.addProperty("totalFeeWaiver", totalWaiver);
            res.addProperty("averageSpi", avgSpi);
            out.print(gson.toJson(res));
        } else {
            List<Scholarship> list = dao.getAllScholarships();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("scholarships", gson.toJsonTree(list));
            out.print(gson.toJson(res));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.contains("all")) {
            // Multithreaded evaluation of all athletes
            List<Scholarship> results = engine.evaluateAllAthletes();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("evaluatedCount", results.size());
            res.addProperty("message", "Multithreaded evaluation completed successfully across all categories.");
            res.add("results", gson.toJsonTree(results));
            out.print(gson.toJson(res));
        } else if (pathInfo != null && pathInfo.length() > 1) {
            // Evaluate single athlete
            String param = pathInfo.substring(1);
            int athleteId = -1;
            try {
                athleteId = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                Athlete a = dao.getAthleteByRegistrationId(param);
                if (a != null) athleteId = a.getAthleteId();
            }

            if (athleteId > 0) {
                Scholarship result = engine.evaluateSingleAthlete(athleteId);
                if (result != null) {
                    JsonObject res = new JsonObject();
                    res.addProperty("success", true);
                    res.add("scholarship", gson.toJsonTree(result));
                    out.print(gson.toJson(res));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Evaluation failed");
                    out.print(gson.toJson(err));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Athlete not found for " + param);
                out.print(gson.toJson(err));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Use POST /api/evaluate/all or POST /api/evaluate/{athleteId}");
            out.print(gson.toJson(err));
        }
    }
}
