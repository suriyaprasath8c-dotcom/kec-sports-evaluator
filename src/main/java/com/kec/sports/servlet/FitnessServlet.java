package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.FitnessLog;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;

@WebServlet("/api/fitness/*")
public class FitnessServlet extends HttpServlet {

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
            List<FitnessLog> list = dao.getAllFitnessLogs();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("logs", gson.toJsonTree(list));
            out.print(gson.toJson(res));
        } else {
            String param = pathInfo.substring(1);
            int athleteId = -1;
            try {
                athleteId = Integer.parseInt(param);
            } catch (NumberFormatException e) {
                Athlete a = dao.getAthleteByRegistrationId(param);
                if (a != null) athleteId = a.getAthleteId();
            }

            if (athleteId > 0) {
                FitnessLog log = dao.getFitnessLogByAthleteId(athleteId);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("fitnessLog", gson.toJsonTree(log));
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Fitness record not found");
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

            int athleteId = -1;
            if (json.has("registrationId")) {
                String regId = json.get("registrationId").getAsString().trim();
                Athlete a = dao.getAthleteByRegistrationId(regId);
                if (a != null) athleteId = a.getAthleteId();
            } else if (json.has("athleteId")) {
                athleteId = json.get("athleteId").getAsInt();
            }

            if (athleteId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Valid Athlete ID or Registration ID required");
                out.print(gson.toJson(err));
                return;
            }

            double fitnessScore = json.get("fitnessScore").getAsDouble();
            double enduranceScore = json.get("enduranceScore").getAsDouble();

            FitnessLog log = new FitnessLog(athleteId, fitnessScore, enduranceScore, new Date(System.currentTimeMillis()));
            int id = dao.insertFitnessLog(log);

            if (id > 0) {
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("fitnessId", id);
                res.addProperty("message", "Fitness score recorded successfully");
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Failed to save fitness score");
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
