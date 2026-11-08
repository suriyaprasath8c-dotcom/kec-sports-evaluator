package com.kec.sports.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kec.sports.dao.SportsDAO;
import com.kec.sports.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet({"/api/login", "/api/logout", "/api/me"})
public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final SportsDAO dao = new SportsDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath();
        if ("/api/logout".equals(servletPath)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("message", "Logged out successfully");
            out.print(gson.toJson(res));
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String body = sb.toString();
            if (body.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Empty request payload");
                out.print(gson.toJson(err));
                return;
            }

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String username = json.has("username") ? json.get("username").getAsString().trim() : "";
            String password = json.has("password") ? json.get("password").getAsString() : "";

            if (username.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Username and password required");
                out.print(gson.toJson(err));
                return;
            }

            User user = dao.authenticateUser(username, password);
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);

                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.addProperty("username", user.getUsername());
                res.addProperty("fullName", user.getFullName());
                res.addProperty("role", user.getRole());
                out.print(gson.toJson(res));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("message", "Invalid username or password");
                out.print(gson.toJson(err));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject err = new JsonObject();
            err.addProperty("success", false);
            err.addProperty("message", "Internal server error: " + e.getMessage());
            out.print(gson.toJson(err));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            JsonObject res = new JsonObject();
            res.addProperty("authenticated", true);
            res.addProperty("username", user.getUsername());
            res.addProperty("fullName", user.getFullName());
            res.addProperty("role", user.getRole());
            out.print(gson.toJson(res));
        } else {
            JsonObject res = new JsonObject();
            res.addProperty("authenticated", false);
            out.print(gson.toJson(res));
        }
    }
}
