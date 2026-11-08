package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.*;
import com.kec.sports.util.StringProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/athletes/*")
public class AthleteServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private final SportsDAO dao = new SportsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            String category = request.getParameter("category");
            String department = request.getParameter("department");
            String search = request.getParameter("search");

            List<Athlete> list;
            if (category != null && !category.trim().isEmpty()) {
                list = dao.getAthletesByCategory(category);
            } else if (department != null && !department.trim().isEmpty()) {
                list = dao.getAthletesByDepartment(department);
            } else if (search != null && !search.trim().isEmpty()) {
                Athlete match = dao.getAthleteByRegistrationId(search.trim());
                list = new ArrayList<>();
                if (match != null) list.add(match);
            } else {
                list = dao.getAllAthletes();
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("count", list.size());
            res.add("athletes", gson.toJsonTree(list));
            out.print(gson.toJson(res));
        } else {
            // /api/athletes/{id or regId}
            String param = pathInfo.substring(1);
            Athlete athlete = null;
            try {
                int id = Integer.parseInt(param);
                athlete = dao.getAthleteById(id);
            } catch (NumberFormatException e) {
                athlete = dao.getAthleteByRegistrationId(param);
            }

            if (athlete != null) {
                FitnessLog fitness = dao.getFitnessLogByAthleteId(athlete.getAthleteId());
                TournamentScore tournament = dao.getTournamentScoreByAthleteId(athlete.getAthleteId());
                TrialLog trial = dao.getTrialLogByAthleteId(athlete.getAthleteId());
                Scholarship scholarship = dao.getScholarshipByAthleteId(athlete.getAthleteId());

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("athlete", gson.toJsonTree(athlete));
                res.add("fitness", gson.toJsonTree(fitness));
                res.add("tournament", gson.toJsonTree(tournament));
                res.add("trial", gson.toJsonTree(trial));
                res.add("scholarship", gson.toJsonTree(scholarship));
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Athlete not found for " + param);
                out.print(gson.toJson(err));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String studentName = json.get("studentName").getAsString().trim();
            String department = json.get("department").getAsString().trim();
            int academicYear = json.get("academicYear").getAsInt();
            int semester = json.get("semester").getAsInt();
            String sportsCategory = json.get("sportsCategory").getAsString().trim();
            String sportsName = json.get("sportsName").getAsString().trim();
            double sgpa = json.get("sgpa").getAsDouble();

            int year = 2026;
            int nextSeq = dao.getNextSequenceNumber(year);
            String regId = StringProcessor.generateRegistrationId(nextSeq, year);
            String eligStatus = (sgpa >= 6.0) ? "ELIGIBLE" : "INELIGIBLE";

            Athlete athlete = new Athlete(regId, studentName, department, academicYear,
                    semester, sportsCategory, sportsName, sgpa, eligStatus);
            int athleteId = dao.insertAthlete(athlete);

            if (athleteId > 0) {
                // Check if fitness scores were provided in the same payload
                if (json.has("fitnessScore") && json.has("enduranceScore")) {
                    double fitScore = json.get("fitnessScore").getAsDouble();
                    double endScore = json.get("enduranceScore").getAsDouble();
                    FitnessLog fLog = new FitnessLog(athleteId, fitScore, endScore, new Date(System.currentTimeMillis()));
                    dao.insertFitnessLog(fLog);
                }

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("athleteId", athleteId);
                res.addProperty("registrationId", regId);
                res.addProperty("eligibilityStatus", eligStatus);
                res.addProperty("message", "Athlete registered successfully");
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Could not register athlete. Duplicate registration ID or DB error.");
                out.print(gson.toJson(err));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Invalid payload: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
}
