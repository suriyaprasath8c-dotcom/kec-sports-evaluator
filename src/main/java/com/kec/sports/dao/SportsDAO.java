package com.kec.sports.dao;

import com.kec.sports.DBConnection;
import com.kec.sports.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * SportsDAO.java
 * Data Access Object for handling all MySQL operations for KEC Sports Evaluator.
 * Uses PreparedStatement to prevent SQL injection and try-with-resources for clean connection closure.
 */
public class SportsDAO {

    // --- Athlete Registration & Management ---

    // Registers a new athlete. Rollbacks if the registration ID already exists.
    public int insertAthlete(Athlete athlete) {
        String checkSql = "SELECT athlete_id FROM athletes WHERE registration_id = ?";
        String insertSql = "INSERT INTO athletes (registration_id, student_name, department, " +
                "academic_year, semester, sports_category, sports_name, sgpa, eligibility_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Duplicate registration ID check
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, athlete.getRegistrationId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        System.err.println("Warning: Athlete ID " + athlete.getRegistrationId() + " already exists.");
                        return -1;
                    }
                }
            }

            // Insert new athlete record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, athlete.getRegistrationId());
                insertStmt.setString(2, athlete.getStudentName());
                insertStmt.setString(3, athlete.getDepartment());
                insertStmt.setInt(4, athlete.getAcademicYear());
                insertStmt.setInt(5, athlete.getSemester());
                insertStmt.setString(6, athlete.getSportsCategory());
                insertStmt.setString(7, athlete.getSportsName());
                insertStmt.setDouble(8, athlete.getSgpa());
                insertStmt.setString(9, athlete.getEligibilityStatus() != null ? athlete.getEligibilityStatus() : "PENDING");

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            int newId = keys.getInt(1);
                            athlete.setAthleteId(newId);
                            conn.commit();
                            return newId;
                        }
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting athlete: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateAthlete(Athlete athlete) {
        String sql = "UPDATE athletes SET student_name = ?, department = ?, academic_year = ?, " +
                "semester = ?, sports_category = ?, sports_name = ?, sgpa = ?, eligibility_status = ? " +
                "WHERE athlete_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, athlete.getStudentName());
            stmt.setString(2, athlete.getDepartment());
            stmt.setInt(3, athlete.getAcademicYear());
            stmt.setInt(4, athlete.getSemester());
            stmt.setString(5, athlete.getSportsCategory());
            stmt.setString(6, athlete.getSportsName());
            stmt.setDouble(7, athlete.getSgpa());
            stmt.setString(8, athlete.getEligibilityStatus());
            stmt.setInt(9, athlete.getAthleteId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating athlete: " + e.getMessage());
            return false;
        }
    }

    public Athlete getAthleteById(int athleteId) {
        String sql = "SELECT * FROM athletes WHERE athlete_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAthlete(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching athlete by ID: " + e.getMessage());
        }
        return null;
    }

    public Athlete getAthleteByRegistrationId(String regId) {
        String sql = "SELECT * FROM athletes WHERE registration_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAthlete(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching athlete by Reg ID: " + e.getMessage());
        }
        return null;
    }

    public List<Athlete> getAllAthletes() {
        List<Athlete> list = new ArrayList<>();
        String sql = "SELECT * FROM athletes ORDER BY athlete_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAthlete(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing athletes: " + e.getMessage());
        }
        return list;
    }

    public List<Athlete> getAthletesByCategory(String category) {
        List<Athlete> list = new ArrayList<>();
        String sql = "SELECT * FROM athletes WHERE sports_category = ? ORDER BY athlete_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAthlete(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering athletes by category: " + e.getMessage());
        }
        return list;
    }

    public List<Athlete> getAthletesByDepartment(String department) {
        List<Athlete> list = new ArrayList<>();
        String sql = "SELECT * FROM athletes WHERE department = ? ORDER BY athlete_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAthlete(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering athletes by dept: " + e.getMessage());
        }
        return list;
    }

    public boolean updateEligibilityStatus(int athleteId, String status) {
        String sql = "UPDATE athletes SET eligibility_status = ? WHERE athlete_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, athleteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating eligibility: " + e.getMessage());
            return false;
        }
    }

    // Auto-calculates the next sequential number for KEC-SPT-{year}-{seq} format
    public int getNextSequenceNumber(int year) {
        String sql = "SELECT registration_id FROM athletes WHERE registration_id LIKE ? ORDER BY athlete_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "KEC-SPT-" + year + "-%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String lastReg = rs.getString("registration_id");
                    String[] parts = lastReg.split("-");
                    if (parts.length == 4) {
                        return Integer.parseInt(parts[3]) + 1;
                    }
                }
            }
        } catch (Exception e) {
            // Default to 1 if no records found
        }
        return 1;
    }

    public int getTotalAthleteCount() {
        return getCount("SELECT COUNT(*) FROM athletes");
    }

    public int getEligibleCount() {
        return getCount("SELECT COUNT(*) FROM athletes WHERE eligibility_status = 'ELIGIBLE'");
    }

    public int getIneligibleCount() {
        return getCount("SELECT COUNT(*) FROM athletes WHERE eligibility_status = 'INELIGIBLE'");
    }

    // --- Phase 1: Fitness & Endurance Logs ---

    public int insertFitnessLog(FitnessLog log) {
        String sql = "INSERT INTO fitness_logs (athlete_id, fitness_score, endurance_score, recorded_date) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, log.getAthleteId());
            stmt.setDouble(2, log.getFitnessScore());
            stmt.setDouble(3, log.getEnduranceScore());
            stmt.setDate(4, log.getRecordedDate() != null ? log.getRecordedDate() : new Date(System.currentTimeMillis()));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        log.setFitnessId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving fitness log: " + e.getMessage());
        }
        return -1;
    }

    public FitnessLog getFitnessLogByAthleteId(int athleteId) {
        String sql = "SELECT * FROM fitness_logs WHERE athlete_id = ? ORDER BY fitness_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapFitnessLog(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching fitness log: " + e.getMessage());
        }
        return null;
    }

    public List<FitnessLog> getAllFitnessLogs() {
        List<FitnessLog> list = new ArrayList<>();
        String sql = "SELECT * FROM fitness_logs ORDER BY fitness_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapFitnessLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing fitness logs: " + e.getMessage());
        }
        return list;
    }

    // --- Phase 2: Tournament Scores ---

    public int insertTournamentScore(TournamentScore score) {
        String sql = "INSERT INTO tournament_scores (athlete_id, tournament_name, tournament_level, " +
                "tournament_points, achievement, teamwork_score) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, score.getAthleteId());
            stmt.setString(2, score.getTournamentName());
            stmt.setString(3, score.getTournamentLevel());
            stmt.setDouble(4, score.getTournamentPoints());
            stmt.setString(5, score.getAchievement());
            stmt.setDouble(6, score.getTeamworkScore());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        score.setTournamentId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving tournament score: " + e.getMessage());
        }
        return -1;
    }

    public TournamentScore getTournamentScoreByAthleteId(int athleteId) {
        String sql = "SELECT * FROM tournament_scores WHERE athlete_id = ? ORDER BY tournament_points DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTournamentScore(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tournament score: " + e.getMessage());
        }
        return null;
    }

    public List<TournamentScore> getAllTournamentScoresByAthleteId(int athleteId) {
        List<TournamentScore> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament_scores WHERE athlete_id = ? ORDER BY tournament_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTournamentScore(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing tournament scores for athlete: " + e.getMessage());
        }
        return list;
    }

    public List<TournamentScore> getAllTournamentScores() {
        List<TournamentScore> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament_scores ORDER BY tournament_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapTournamentScore(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing all tournament scores: " + e.getMessage());
        }
        return list;
    }

    // --- Phase 3: Track & Field Trial Timings ---

    public int insertTrialLog(TrialLog log) {
        String sql = "INSERT INTO trial_logs (athlete_id, event_name, trial_timing, " +
                "normalized_trial_score, discipline_score, coach_remarks) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, log.getAthleteId());
            stmt.setString(2, log.getEventName());
            stmt.setString(3, log.getTrialTiming());
            stmt.setDouble(4, log.getNormalizedTrialScore());
            stmt.setDouble(5, log.getDisciplineScore());
            stmt.setString(6, log.getCoachRemarks());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        log.setTrialId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving trial log: " + e.getMessage());
        }
        return -1;
    }

    public TrialLog getTrialLogByAthleteId(int athleteId) {
        String sql = "SELECT * FROM trial_logs WHERE athlete_id = ? ORDER BY normalized_trial_score DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTrialLog(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trial log: " + e.getMessage());
        }
        return null;
    }

    public List<TrialLog> getAllTrialLogsByAthleteId(int athleteId) {
        List<TrialLog> list = new ArrayList<>();
        String sql = "SELECT * FROM trial_logs WHERE athlete_id = ? ORDER BY trial_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrialLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing trial logs: " + e.getMessage());
        }
        return list;
    }

    public List<TrialLog> getAllTrialLogs() {
        List<TrialLog> list = new ArrayList<>();
        String sql = "SELECT * FROM trial_logs ORDER BY trial_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapTrialLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing all trial logs: " + e.getMessage());
        }
        return list;
    }

    // --- Scholarships & Evaluation Persistence ---

    // Uses ON DUPLICATE KEY UPDATE so athletes can be re-evaluated cleanly
    public boolean insertOrUpdateScholarship(Scholarship scholarship) {
        String sql = "INSERT INTO scholarships (athlete_id, fitness_score, tournament_score, " +
                "trial_score, discipline_score, teamwork_score, spi, scholarship_type, fee_waiver, " +
                "evaluation_status, evaluation_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?) " +
                "ON DUPLICATE KEY UPDATE fitness_score = VALUES(fitness_score), tournament_score = VALUES(tournament_score), " +
                "trial_score = VALUES(trial_score), discipline_score = VALUES(discipline_score), " +
                "teamwork_score = VALUES(teamwork_score), spi = VALUES(spi), " +
                "scholarship_type = VALUES(scholarship_type), fee_waiver = VALUES(fee_waiver), " +
                "evaluation_status = VALUES(evaluation_status), evaluation_date = CURRENT_TIMESTAMP, remarks = VALUES(remarks)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scholarship.getAthleteId());
            stmt.setDouble(2, scholarship.getFitnessScore());
            stmt.setDouble(3, scholarship.getTournamentScore());
            stmt.setDouble(4, scholarship.getTrialScore());
            stmt.setDouble(5, scholarship.getDisciplineScore());
            stmt.setDouble(6, scholarship.getTeamworkScore());
            stmt.setDouble(7, scholarship.getSpi());
            stmt.setString(8, scholarship.getScholarshipType());
            stmt.setDouble(9, scholarship.getFeeWaiver());
            stmt.setString(10, scholarship.getEvaluationStatus());
            stmt.setString(11, scholarship.getRemarks());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving scholarship: " + e.getMessage());
            return false;
        }
    }

    public Scholarship getScholarshipByAthleteId(int athleteId) {
        String sql = "SELECT s.*, a.registration_id, a.student_name, a.department, a.sports_category, " +
                "a.sports_name, a.sgpa FROM scholarships s " +
                "JOIN athletes a ON s.athlete_id = a.athlete_id WHERE s.athlete_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, athleteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapScholarship(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching scholarship: " + e.getMessage());
        }
        return null;
    }

    public List<Scholarship> getAllScholarships() {
        List<Scholarship> list = new ArrayList<>();
        String sql = "SELECT s.*, a.registration_id, a.student_name, a.department, a.sports_category, " +
                "a.sports_name, a.sgpa FROM scholarships s " +
                "JOIN athletes a ON s.athlete_id = a.athlete_id ORDER BY s.spi DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapScholarship(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing scholarships: " + e.getMessage());
        }
        return list;
    }

    public double getTotalFeeWaiver() {
        String sql = "SELECT SUM(fee_waiver) FROM scholarships";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total fee waiver: " + e.getMessage());
        }
        return 0.0;
    }

    public Map<String, Integer> getScholarshipSummary() {
        Map<String, Integer> map = new HashMap<>();
        map.put("GOLD", 0);
        map.put("SILVER", 0);
        map.put("BRONZE", 0);
        map.put("NO_AWARD", 0);

        String sql = "SELECT scholarship_type, COUNT(*) as cnt FROM scholarships GROUP BY scholarship_type";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("scholarship_type"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting scholarship summary: " + e.getMessage());
        }
        return map;
    }

    // --- Authentication ---

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = SHA2(?, 256)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login authentication: " + e.getMessage());
        }
        return null;
    }

    // --- Row Mappers ---

    private Athlete mapAthlete(ResultSet rs) throws SQLException {
        Athlete a = new Athlete();
        a.setAthleteId(rs.getInt("athlete_id"));
        a.setRegistrationId(rs.getString("registration_id"));
        a.setStudentName(rs.getString("student_name"));
        a.setDepartment(rs.getString("department"));
        a.setAcademicYear(rs.getInt("academic_year"));
        a.setSemester(rs.getInt("semester"));
        a.setSportsCategory(rs.getString("sports_category"));
        a.setSportsName(rs.getString("sports_name"));
        a.setSgpa(rs.getDouble("sgpa"));
        a.setEligibilityStatus(rs.getString("eligibility_status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));
        return a;
    }

    private FitnessLog mapFitnessLog(ResultSet rs) throws SQLException {
        FitnessLog f = new FitnessLog();
        f.setFitnessId(rs.getInt("fitness_id"));
        f.setAthleteId(rs.getInt("athlete_id"));
        f.setFitnessScore(rs.getDouble("fitness_score"));
        f.setEnduranceScore(rs.getDouble("endurance_score"));
        f.setRecordedDate(rs.getDate("recorded_date"));
        f.setCreatedAt(rs.getTimestamp("created_at"));
        return f;
    }

    private TournamentScore mapTournamentScore(ResultSet rs) throws SQLException {
        TournamentScore t = new TournamentScore();
        t.setTournamentId(rs.getInt("tournament_id"));
        t.setAthleteId(rs.getInt("athlete_id"));
        t.setTournamentName(rs.getString("tournament_name"));
        t.setTournamentLevel(rs.getString("tournament_level"));
        t.setTournamentPoints(rs.getDouble("tournament_points"));
        t.setAchievement(rs.getString("achievement"));
        t.setTeamworkScore(rs.getDouble("teamwork_score"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }

    private TrialLog mapTrialLog(ResultSet rs) throws SQLException {
        TrialLog tr = new TrialLog();
        tr.setTrialId(rs.getInt("trial_id"));
        tr.setAthleteId(rs.getInt("athlete_id"));
        tr.setEventName(rs.getString("event_name"));
        tr.setTrialTiming(rs.getString("trial_timing"));
        tr.setNormalizedTrialScore(rs.getDouble("normalized_trial_score"));
        tr.setDisciplineScore(rs.getDouble("discipline_score"));
        tr.setCoachRemarks(rs.getString("coach_remarks"));
        tr.setCreatedAt(rs.getTimestamp("created_at"));
        return tr;
    }

    private Scholarship mapScholarship(ResultSet rs) throws SQLException {
        Scholarship s = new Scholarship();
        s.setScholarshipId(rs.getInt("scholarship_id"));
        s.setAthleteId(rs.getInt("athlete_id"));
        s.setFitnessScore(rs.getDouble("fitness_score"));
        s.setTournamentScore(rs.getDouble("tournament_score"));
        s.setTrialScore(rs.getDouble("trial_score"));
        s.setDisciplineScore(rs.getDouble("discipline_score"));
        s.setTeamworkScore(rs.getDouble("teamwork_score"));
        s.setSpi(rs.getDouble("spi"));
        s.setScholarshipType(rs.getString("scholarship_type"));
        s.setFeeWaiver(rs.getDouble("fee_waiver"));
        s.setEvaluationStatus(rs.getString("evaluation_status"));
        s.setEvaluationDate(rs.getTimestamp("evaluation_date"));
        s.setRemarks(rs.getString("remarks"));

        try {
            s.setRegistrationId(rs.getString("registration_id"));
            s.setStudentName(rs.getString("student_name"));
            s.setDepartment(rs.getString("department"));
            s.setSportsCategory(rs.getString("sports_category"));
            s.setSportsName(rs.getString("sports_name"));
            s.setSgpa(rs.getDouble("sgpa"));
        } catch (SQLException ignored) {
            // Joined columns might not be present in raw SELECT *
        }
        return s;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }

    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting rows: " + e.getMessage());
        }
        return 0;
    }
}
