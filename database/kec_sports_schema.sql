-- ============================================================================
-- KEC Student Sports Athletic Performance & Scholarship Evaluator
-- Database Schema for MySQL
-- ============================================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS kec_sports_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kec_sports_db;

-- ============================================================================
-- Table 1: users
-- Stores admin and evaluator login credentials
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100),
    role          ENUM('ADMIN', 'EVALUATOR') DEFAULT 'EVALUATOR',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- Table 2: athletes
-- Core table holding every registered student-athlete's profile
-- ============================================================================
CREATE TABLE IF NOT EXISTS athletes (
    athlete_id        INT AUTO_INCREMENT PRIMARY KEY,
    registration_id   VARCHAR(20)  UNIQUE NOT NULL,
    student_name      VARCHAR(100) NOT NULL,
    department        VARCHAR(50)  NOT NULL,
    academic_year     INT          NOT NULL,
    semester          INT          NOT NULL,
    sports_category   ENUM('INDIVIDUAL_SPORTS', 'TEAM_SPORTS', 'TRACK_AND_FIELD') NOT NULL,
    sports_name       VARCHAR(100) NOT NULL,
    sgpa              DECIMAL(4,2),
    eligibility_status ENUM('ELIGIBLE', 'INELIGIBLE', 'PENDING') DEFAULT 'PENDING',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================================
-- Table 3: fitness_logs
-- Physical fitness assessment records for each athlete
-- ============================================================================
CREATE TABLE IF NOT EXISTS fitness_logs (
    fitness_id      INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id      INT NOT NULL,
    fitness_score   DECIMAL(5,2) CHECK (fitness_score >= 0 AND fitness_score <= 100),
    endurance_score DECIMAL(5,2) CHECK (endurance_score >= 0 AND endurance_score <= 100),
    recorded_date   DATE NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fitness_athlete
        FOREIGN KEY (athlete_id) REFERENCES athletes(athlete_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- Table 4: tournament_scores
-- Records from zonal, inter-college, state, and national tournaments
-- ============================================================================
CREATE TABLE IF NOT EXISTS tournament_scores (
    tournament_id     INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id        INT NOT NULL,
    tournament_name   VARCHAR(200) NOT NULL,
    tournament_level  ENUM('ZONAL', 'INTER_COLLEGE', 'STATE', 'NATIONAL') NOT NULL,
    tournament_points DECIMAL(5,2) CHECK (tournament_points >= 0 AND tournament_points <= 100),
    achievement       VARCHAR(100),
    teamwork_score    DECIMAL(5,2) CHECK (teamwork_score >= 0 AND teamwork_score <= 100),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournament_athlete
        FOREIGN KEY (athlete_id) REFERENCES athletes(athlete_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- Table 5: trial_logs
-- Time-trial results for track-and-field athletes with coach feedback
-- ============================================================================
CREATE TABLE IF NOT EXISTS trial_logs (
    trial_id              INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id            INT NOT NULL,
    event_name            VARCHAR(100) NOT NULL,
    trial_timing          VARCHAR(20)  NOT NULL COMMENT 'Format: MM:SS.ms (e.g. 02:15.340)',
    normalized_trial_score DECIMAL(5,2),
    discipline_score      DECIMAL(5,2) CHECK (discipline_score >= 0 AND discipline_score <= 100),
    coach_remarks         TEXT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trial_athlete
        FOREIGN KEY (athlete_id) REFERENCES athletes(athlete_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- Table 6: scholarships
-- Final evaluated scholarship outcome per athlete (one row per athlete)
-- ============================================================================
CREATE TABLE IF NOT EXISTS scholarships (
    scholarship_id    INT AUTO_INCREMENT PRIMARY KEY,
    athlete_id        INT UNIQUE NOT NULL,
    fitness_score     DECIMAL(5,2),
    tournament_score  DECIMAL(5,2),
    trial_score       DECIMAL(5,2),
    discipline_score  DECIMAL(5,2),
    teamwork_score    DECIMAL(5,2),
    spi               DECIMAL(5,2) COMMENT 'Sports Performance Index – weighted composite score',
    scholarship_type  ENUM('GOLD', 'SILVER', 'BRONZE', 'NO_AWARD') DEFAULT 'NO_AWARD',
    fee_waiver        DECIMAL(5,2) DEFAULT 0.00,
    evaluation_status ENUM('EVALUATED', 'PENDING', 'INELIGIBLE') DEFAULT 'PENDING',
    evaluation_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks           TEXT,
    CONSTRAINT fk_scholarship_athlete
        FOREIGN KEY (athlete_id) REFERENCES athletes(athlete_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
-- Indexes for faster lookups
-- ============================================================================
CREATE INDEX idx_athletes_registration_id   ON athletes(registration_id);
CREATE INDEX idx_athletes_sports_category   ON athletes(sports_category);
CREATE INDEX idx_athletes_eligibility       ON athletes(eligibility_status);

CREATE INDEX idx_fitness_athlete_id         ON fitness_logs(athlete_id);
CREATE INDEX idx_tournament_athlete_id      ON tournament_scores(athlete_id);
CREATE INDEX idx_trial_athlete_id           ON trial_logs(athlete_id);

CREATE INDEX idx_scholarships_type          ON scholarships(scholarship_type);
CREATE INDEX idx_scholarships_athlete_id    ON scholarships(athlete_id);

-- ============================================================================
-- Sample Data: Users
-- Passwords hashed with SHA2-256
-- ============================================================================
INSERT INTO users (username, password_hash, full_name, role) VALUES
    ('admin',     SHA2('admin123', 256), 'Dr. S. Ramesh Kumar',   'ADMIN'),
    ('evaluator', SHA2('eval123',  256), 'Prof. M. Lakshmi Devi', 'EVALUATOR');

-- ============================================================================
-- Sample Data: Athletes (6 athletes, 2 per sports category)
-- NOTE: Athletes 3 and 6 have SGPA < 6.0 to test INELIGIBLE logic
-- ============================================================================
INSERT INTO athletes (registration_id, student_name, department, academic_year, semester,
                      sports_category, sports_name, sgpa, eligibility_status) VALUES
    -- INDIVIDUAL_SPORTS (2)
    ('KEC-SPT-2026-001', 'Arun Prakash R',    'CSE',   3, 5, 'INDIVIDUAL_SPORTS', 'Badminton',        8.20, 'ELIGIBLE'),
    ('KEC-SPT-2026-002', 'Divya Shree S',     'ECE',   2, 4, 'INDIVIDUAL_SPORTS', 'Table Tennis',     7.45, 'ELIGIBLE'),
    -- TEAM_SPORTS (2)
    ('KEC-SPT-2026-003', 'Karthik Venkat M',  'MECH',  3, 6, 'TEAM_SPORTS',       'Cricket',          5.80, 'INELIGIBLE'),
    ('KEC-SPT-2026-004', 'Priya Dharshini K', 'EEE',   2, 3, 'TEAM_SPORTS',       'Volleyball',       7.90, 'ELIGIBLE'),
    -- TRACK_AND_FIELD (2)
    ('KEC-SPT-2026-005', 'Suresh Babu G',     'IT',    4, 7, 'TRACK_AND_FIELD',   '100m Sprint',      6.50, 'ELIGIBLE'),
    ('KEC-SPT-2026-006', 'Meena Kumari T',    'CIVIL', 2, 4, 'TRACK_AND_FIELD',   '400m Relay',       5.20, 'INELIGIBLE');

-- ============================================================================
-- Sample Data: Fitness Logs (one record per athlete)
-- ============================================================================
INSERT INTO fitness_logs (athlete_id, fitness_score, endurance_score, recorded_date) VALUES
    (1, 85.50, 78.00, '2026-08-15'),
    (2, 72.00, 80.50, '2026-08-16'),
    (3, 68.75, 65.00, '2026-08-17'),
    (4, 90.00, 88.25, '2026-08-18'),
    (5, 82.30, 91.00, '2026-08-19'),
    (6, 60.00, 55.50, '2026-08-20');

-- ============================================================================
-- Sample Data: Tournament Scores (one tournament record per athlete)
-- ============================================================================
INSERT INTO tournament_scores (athlete_id, tournament_name, tournament_level,
                               tournament_points, achievement, teamwork_score) VALUES
    (1, 'Anna University Zonal Badminton Meet 2026',        'ZONAL',         78.50, 'Quarter-Finalist',  65.00),
    (2, 'Tamil Nadu Inter-College Table Tennis League 2026', 'INTER_COLLEGE', 82.00, 'Semi-Finalist',     70.00),
    (3, 'KEC Inter-Dept Cricket Tournament 2026',           'ZONAL',         55.00, 'Participant',       80.50),
    (4, 'South Zone Volleyball Championship 2026',          'STATE',         91.25, 'Gold Medalist',     95.00),
    (5, 'Tamil Nadu State Athletics Meet 2026',             'STATE',         88.00, 'Silver Medalist',   60.00),
    (6, 'Anna University National Relay Trials 2026',       'NATIONAL',      45.75, 'Participant',       72.00);

-- ============================================================================
-- Sample Data: Trial Logs (only for Track & Field athletes – IDs 5 and 6)
-- ============================================================================
INSERT INTO trial_logs (athlete_id, event_name, trial_timing, normalized_trial_score,
                        discipline_score, coach_remarks) VALUES
    (5, '100m Sprint Trial',  '00:11.340', 85.00, 90.00,
     'Excellent start reaction. Needs marginal improvement in finishing stride.'),
    (6, '400m Relay Trial',   '01:02.780', 58.50, 62.00,
     'Baton exchange needs practice. Stamina below average; recommend extra conditioning.');

-- ============================================================================
-- End of schema
-- ============================================================================
