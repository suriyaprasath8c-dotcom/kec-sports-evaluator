package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.TournamentScore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/tournaments/*")
public class TournamentServlet extends HttpServlet {

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
            List<TournamentScore> list = dao.getAllTournamentScores();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("scores", gson.toJsonTree(list));
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
                List<TournamentScore> scores = dao.getAllTournamentScoresByAthleteId(athleteId);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("scores", gson.toJsonTree(scores));
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Tournament scores not found");
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
                err.addProperty("message", "Athlete not found");
                out.print(gson.toJson(err));
                return;
            }

            String tournamentName = json.get("tournamentName").getAsString().trim();
            String tournamentLevel = json.get("tournamentLevel").getAsString().trim().toUpperCase();
            double tournamentPoints = json.get("tournamentPoints").getAsDouble();
            String achievement = json.has("achievement") ? json.get("achievement").getAsString().trim() : "";
            double teamworkScore = json.has("teamworkScore") ? json.get("teamworkScore").getAsDouble() : 0.0;

            TournamentScore score = new TournamentScore(athleteId, tournamentName, tournamentLevel,
                    tournamentPoints, achievement, teamworkScore);
            int id = dao.insertTournamentScore(score);

            if (id > 0) {
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("tournamentId", id);
                res.addProperty("message", "Tournament performance recorded successfully");
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Failed to save tournament score");
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
