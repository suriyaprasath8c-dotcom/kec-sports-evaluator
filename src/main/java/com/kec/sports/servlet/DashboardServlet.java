package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.Scholarship;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private final SportsDAO dao = new SportsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int totalAthletes = dao.getTotalAthleteCount();
            int eligibleCount = dao.getEligibleCount();
            int ineligibleCount = dao.getIneligibleCount();
            double totalFeeWaiver = dao.getTotalFeeWaiver();
            Map<String, Integer> scholarshipSummary = dao.getScholarshipSummary();

            List<Athlete> allAthletes = dao.getAllAthletes();
            Map<String, Integer> categoryDistribution = new HashMap<>();
            categoryDistribution.put("INDIVIDUAL_SPORTS", 0);
            categoryDistribution.put("TEAM_SPORTS", 0);
            categoryDistribution.put("TRACK_AND_FIELD", 0);

            Map<String, Integer> deptDistribution = new HashMap<>();

            for (Athlete a : allAthletes) {
                String cat = a.getSportsCategory();
                categoryDistribution.put(cat, categoryDistribution.getOrDefault(cat, 0) + 1);

                String dept = a.getDepartment();
                deptDistribution.put(dept, deptDistribution.getOrDefault(dept, 0) + 1);
            }

            List<Scholarship> allScholarships = dao.getAllScholarships();

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("totalAthletes", totalAthletes);
            res.addProperty("eligibleAthletes", eligibleCount);
            res.addProperty("ineligibleAthletes", ineligibleCount);
            res.addProperty("totalFeeWaiver", totalFeeWaiver);
            res.addProperty("goldCount", scholarshipSummary.getOrDefault("GOLD", 0));
            res.addProperty("silverCount", scholarshipSummary.getOrDefault("SILVER", 0));
            res.addProperty("bronzeCount", scholarshipSummary.getOrDefault("BRONZE", 0));
            res.addProperty("noAwardCount", scholarshipSummary.getOrDefault("NO_AWARD", 0));

            res.add("categoryDistribution", gson.toJsonTree(categoryDistribution));
            res.add("departmentDistribution", gson.toJsonTree(deptDistribution));
            res.add("scholarships", gson.toJsonTree(allScholarships));

            out.print(gson.toJson(res));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Dashboard data error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
}
