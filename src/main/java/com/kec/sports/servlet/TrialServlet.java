package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.TrialLog;
import com.kec.sports.util.CSVHandler;
import com.kec.sports.util.StringProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/trials/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class TrialServlet extends HttpServlet {

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
            List<TrialLog> list = dao.getAllTrialLogs();
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("trials", gson.toJsonTree(list));
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
                List<TrialLog> list = dao.getAllTrialLogsByAthleteId(athleteId);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("trials", gson.toJsonTree(list));
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Trial logs not found");
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

        String pathInfo = request.getPathInfo();

        // Check if CSV Import endpoint
        if (pathInfo != null && pathInfo.contains("import")) {
            handleCsvImport(request, response, out);
            return;
        }

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

            String eventName = json.get("eventName").getAsString().trim();
            String trialTiming = json.get("trialTiming").getAsString().trim();

            if (!StringProcessor.validateTrialTiming(trialTiming)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Invalid timing format! Expected MM:SS.ms (e.g. 00:11.25 or 02:15.45)");
                out.print(gson.toJson(err));
                return;
            }

            double actualSec = StringProcessor.parseTrialTiming(trialTiming);
            double benchmarkSec = StringProcessor.getBenchmarkTime(eventName);
            double normalizedScore = StringProcessor.normalizeTrialScore(actualSec, benchmarkSec);

            double disciplineScore = json.has("disciplineScore") ? json.get("disciplineScore").getAsDouble() : 85.0;
            String rawRemarks = json.has("coachRemarks") ? json.get("coachRemarks").getAsString() : "";
            String sanitizedRemarks = StringProcessor.sanitizeCoachRemarks(rawRemarks);

            TrialLog log = new TrialLog(athleteId, eventName, trialTiming, normalizedScore, disciplineScore, sanitizedRemarks);
            int id = dao.insertTrialLog(log);

            if (id > 0) {
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("trialId", id);
                res.addProperty("normalizedScore", normalizedScore);
                res.addProperty("message", "Trial log recorded successfully");
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Failed to save trial log");
                out.print(gson.toJson(err));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Error processing trial log: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }

    private void handleCsvImport(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try {
            List<TrialLog> importedLogs = new ArrayList<>();
            String contentType = request.getContentType();

            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
                Part filePart = request.getPart("file");
                if (filePart != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(filePart.getInputStream()))) {
                        String line = reader.readLine(); // skip header
                        int lineNo = 1;
                        while ((line = reader.readLine()) != null) {
                            lineNo++;
                            if (line.trim().isEmpty()) continue;
                            String[] parts = line.split(",", -1);
                            if (parts.length >= 4) {
                                String regId = parts[0].trim();
                                String event = parts[1].trim();
                                String timing = parts[2].trim();
                                double disc = Double.parseDouble(parts[3].trim());
                                String rem = parts.length > 4 ? parts[4].trim() : "";
                                importedLogs.add(new TrialLog(regId, event, timing, disc, rem));
                            }
                        }
                    }
                }
            } else {
                // If not multipart, check standard uploads/trial_timings.csv
                File defaultCsv = new File("uploads/trial_timings.csv");
                if (defaultCsv.exists()) {
                    importedLogs = CSVHandler.importTrialTimings(defaultCsv.getAbsolutePath());
                }
            }

            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            for (TrialLog log : importedLogs) {
                if (log.getRegistrationId() != null) {
                    Athlete athlete = dao.getAthleteByRegistrationId(log.getRegistrationId());
                    if (athlete != null) {
                        try {
                            log.setAthleteId(athlete.getAthleteId());
                            double actualSec = StringProcessor.parseTrialTiming(log.getTrialTiming());
                            double bench = StringProcessor.getBenchmarkTime(log.getEventName());
                            log.setNormalizedTrialScore(StringProcessor.normalizeTrialScore(actualSec, bench));
                            log.setCoachRemarks(StringProcessor.sanitizeCoachRemarks(log.getCoachRemarks()));
                            int id = dao.insertTrialLog(log);
                            if (id > 0) successCount++;
                            else errorCount++;
                        } catch (Exception ex) {
                            errorCount++;
                            errors.add("Row for " + log.getRegistrationId() + ": " + ex.getMessage());
                        }
                    } else {
                        errorCount++;
                        errors.add("Athlete Registration ID not found: " + log.getRegistrationId());
                    }
                }
            }

            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("importedCount", successCount);
            res.addProperty("errorCount", errorCount);
            res.add("errors", gson.toJsonTree(errors));
            res.addProperty("message", "Import processed: " + successCount + " records added to database.");
            out.print(gson.toJson(res));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "CSV Import error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
}
