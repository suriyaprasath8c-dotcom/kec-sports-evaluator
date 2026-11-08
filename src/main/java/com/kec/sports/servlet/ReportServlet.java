package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.evaluation.EvaluationEngine;
import com.kec.sports.export.ReportExporter;
import com.kec.sports.model.Athlete;
import com.kec.sports.model.Scholarship;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final SportsDAO dao = new SportsDAO();
    private final ReportExporter exporter = new ReportExporter();
    private final EvaluationEngine engine = new EvaluationEngine(dao);
    private static final String REPORTS_DIR = "reports";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/download/")) {
            String fileName = pathInfo.substring("/download/".length());
            // Sanitize filename against traversal
            fileName = new File(fileName).getName();
            File file = new File(REPORTS_DIR, fileName);

            if (file.exists() && file.isFile()) {
                response.setContentType("text/plain");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                response.setContentLength((int) file.length());
                try (InputStream in = new FileInputStream(file);
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Report file not found\"}");
            }
        } else {
            // List generated report files
            File dir = new File(REPORTS_DIR);
            String[] files = dir.exists() ? dir.list((d, name) -> name.endsWith(".txt")) : new String[0];
            response.setContentType("application/json");
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("files", gson.toJsonTree(files != null ? files : new String[0]));
            response.getWriter().write(gson.toJson(res));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo != null && pathInfo.startsWith("/letter/")) {
                String param = pathInfo.substring("/letter/".length());
                int athleteId = -1;
                try {
                    athleteId = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    Athlete a = dao.getAthleteByRegistrationId(param);
                    if (a != null) athleteId = a.getAthleteId();
                }

                Athlete athlete = dao.getAthleteById(athleteId);
                if (athlete == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject err = new JsonObject();
                    err.addProperty("success", false);
                    err.addProperty("message", "Athlete not found");
                    out.print(gson.toJson(err));
                    return;
                }

                Scholarship scholarship = dao.getScholarshipByAthleteId(athleteId);
                if (scholarship == null) {
                    scholarship = engine.evaluateSingleAthlete(athleteId);
                }

                String filePath = exporter.generateAwardLetter(athlete, scholarship, REPORTS_DIR);
                File letterFile = new File(filePath);
                String content = new String(Files.readAllBytes(letterFile.toPath()));

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("fileName", letterFile.getName());
                res.addProperty("filePath", filePath);
                res.addProperty("content", content);
                res.addProperty("downloadUrl", "/api/reports/download/" + letterFile.getName());
                out.print(gson.toJson(res));

            } else if (pathInfo != null && pathInfo.contains("all-letters")) {
                List<Athlete> athletes = dao.getAllAthletes();
                List<Scholarship> scholarships = dao.getAllScholarships();
                if (scholarships.isEmpty()) {
                    scholarships = engine.evaluateAllAthletes();
                }

                int count = exporter.generateAllAwardLetters(athletes, scholarships, REPORTS_DIR);
                String summaryPath = exporter.generateEvaluationSummary(scholarships, REPORTS_DIR);

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("lettersGenerated", count);
                res.addProperty("summaryReport", new File(summaryPath).getName());
                res.addProperty("message", "Generated " + count + " letters and evaluation summary successfully.");
                out.print(gson.toJson(res));

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Invalid endpoint. Use /api/reports/letter/{id} or /api/reports/all-letters");
                out.print(gson.toJson(err));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Report generation error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }
}
