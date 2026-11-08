# Database Setup & Configuration Guide
## KEC Student Sports Athletic Performance & Scholarship Evaluator

---

## 1. Database Overview

The persistence tier runs on **MySQL 8.0 Community Server** hosted on **OCI VM 2 (Database Host)**.
- **Database Name**: `kec_sports_db`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`
- **Default Port**: `3306`

---

## 2. Installation on OCI VM 2 (Oracle Linux 8 / Ubuntu)

### Option A: Oracle Linux 8 / RHEL 8
```bash
# Update repository packages
sudo dnf update -y

# Enable MySQL 8.0 module stream
sudo dnf module enable mysql:8.0 -y

# Install MySQL server
sudo dnf install mysql-server -y

# Enable and start mysqld service
sudo systemctl enable --now mysqld
sudo systemctl status mysqld
```

### Option B: Ubuntu 22.04 LTS
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install mysql-server -y
sudo systemctl enable --now mysql
```

---

## 3. Remote MySQL Configuration for OCI VM 1

By default, MySQL listens only on `127.0.0.1`. To allow OCI VM 1 (`10.0.1.10`) to connect over the private VCN:

1. Edit the MySQL configuration file:
   - Oracle Linux: `/etc/my.cnf.d/mysql-server.cnf`
   - Ubuntu: `/etc/mysql/mysql.conf.d/mysqld.cnf`

2. Set the `bind-address`:
   ```ini
   [mysqld]
   bind-address = 0.0.0.0
   port = 3306
   max_connections = 150
   default_authentication_plugin = mysql_native_password
   ```

3. Restart the MySQL daemon:
   ```bash
   sudo systemctl restart mysqld  # (or mysql on Ubuntu)
   ```

---

## 4. Database Creation & User Provisioning

Log in to MySQL as root:
```bash
sudo mysql -u root
```

Execute the user setup and privilege grants:
```sql
-- Create database
CREATE DATABASE IF NOT EXISTS kec_sports_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Create dedicated application user restricted to VM 1 private IP (10.0.1.10)
CREATE USER IF NOT EXISTS 'kec_sports_admin'@'10.0.1.10' IDENTIFIED BY 'SecureKECPassword2026!';
GRANT ALL PRIVILEGES ON kec_sports_db.* TO 'kec_sports_admin'@'10.0.1.10';

-- For local development / testing
CREATE USER IF NOT EXISTS 'kec_user'@'localhost' IDENTIFIED BY 'kec_sports_2026';
GRANT ALL PRIVILEGES ON kec_sports_db.* TO 'kec_user'@'localhost';

FLUSH PRIVILEGES;
```

---

## 5. Schema Execution (`kec_sports_schema.sql`)

Import the complete database schema from `database/kec_sports_schema.sql`:

```bash
mysql -u kec_user -p kec_sports_db < database/kec_sports_schema.sql
```

Or execute within the MySQL shell:
```sql
USE kec_sports_db;
SOURCE /path/to/kec-sports-evaluator/database/kec_sports_schema.sql;
```

---

## 6. Table Structures & Constraints

### 1. `users` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `user_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Authentication username |
| `password_hash` | VARCHAR(255) | NOT NULL | SHA2-256 password hash |
| `full_name` | VARCHAR(100) | | Full name of staff member |
| `role` | ENUM | 'ADMIN', 'EVALUATOR' | Authorization role |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 2. `athletes` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `athlete_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Internal primary key |
| `registration_id` | VARCHAR(20) | UNIQUE, NOT NULL | Format: `KEC-SPT-YYYY-NNN` |
| `student_name` | VARCHAR(100) | NOT NULL | Full student name |
| `department` | VARCHAR(50) | NOT NULL | CSE, ECE, MECH, etc. |
| `academic_year` | INT | NOT NULL (1 - 4) | Year of study |
| `semester` | INT | NOT NULL (1 - 8) | Current semester |
| `sports_category` | ENUM | NOT NULL | INDIVIDUAL_SPORTS, TEAM_SPORTS, TRACK_AND_FIELD |
| `sports_name` | VARCHAR(100) | NOT NULL | Badminton, Cricket, etc. |
| `sgpa` | DECIMAL(4,2) | NOT NULL | Academic SGPA |
| `eligibility_status`| ENUM | 'ELIGIBLE', 'INELIGIBLE', 'PENDING' | Academic eligibility flag |

### 3. `fitness_logs` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `fitness_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Assessment identifier |
| `athlete_id` | INT | FOREIGN KEY (athletes) ON DELETE CASCADE | Associated athlete |
| `fitness_score` | DECIMAL(5,2) | CHECK (0 - 100) | Physical fitness test score |
| `endurance_score`| DECIMAL(5,2) | CHECK (0 - 100) | Endurance score |
| `recorded_date` | DATE | NOT NULL | Assessment date |

### 4. `tournament_scores` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `tournament_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Achievement identifier |
| `athlete_id` | INT | FOREIGN KEY (athletes) ON DELETE CASCADE | Associated athlete |
| `tournament_name`| VARCHAR(200)| NOT NULL | Name of tournament |
| `tournament_level`| ENUM | 'ZONAL', 'INTER_COLLEGE', 'STATE', 'NATIONAL' | Competition level |
| `tournament_points`| DECIMAL(5,2)| CHECK (0 - 100) | Points earned |
| `achievement` | VARCHAR(100) | | Gold Medalist, Semi-Finalist, etc. |
| `teamwork_score`| DECIMAL(5,2) | CHECK (0 - 100) | Team dynamics score |

### 5. `trial_logs` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `trial_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Trial identifier |
| `athlete_id` | INT | FOREIGN KEY (athletes) ON DELETE CASCADE | Associated athlete |
| `event_name` | VARCHAR(100) | NOT NULL | 100m Sprint, 400m Relay |
| `trial_timing` | VARCHAR(20) | NOT NULL | Format: `MM:SS.ms` |
| `normalized_trial_score` | DECIMAL(5,2) | | Converted 0 - 100 score |
| `discipline_score` | DECIMAL(5,2) | CHECK (0 - 100) | Athlete discipline rating |
| `coach_remarks` | TEXT | | Sanitized coach remarks |

### 6. `scholarships` Table
| Column | Type | Constraints | Description |
|:---|:---|:---|:---|
| `scholarship_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Award record ID |
| `athlete_id` | INT | UNIQUE, FOREIGN KEY (athletes) ON DELETE CASCADE | One-to-one athlete evaluation |
| `fitness_score` | DECIMAL(5,2) | | Final fitness component score |
| `tournament_score`| DECIMAL(5,2)| | Final tournament component score |
| `trial_score` | DECIMAL(5,2) | | Final trial component score |
| `discipline_score`| DECIMAL(5,2)| | Final discipline component score |
| `teamwork_score`| DECIMAL(5,2) | | Final teamwork component score |
| `spi` | DECIMAL(5,2) | | Sports Performance Index (%) |
| `scholarship_type`| ENUM | 'GOLD', 'SILVER', 'BRONZE', 'NO_AWARD' | Award tier |
| `fee_waiver` | DECIMAL(5,2) | 100.0, 50.0, 25.0, 0.0 | Institutional fee waiver percentage |
| `evaluation_status`| ENUM | 'EVALUATED', 'PENDING', 'INELIGIBLE' | Evaluation status |
| `remarks` | TEXT | | Evaluation rationale |

---

## 7. Verification Queries

```sql
-- 1. Check total athlete count
SELECT COUNT(*) FROM athletes;

-- 2. Check academic eligibility status breakdown
SELECT eligibility_status, COUNT(*) FROM athletes GROUP BY eligibility_status;

-- 3. View evaluated scholarship tiers
SELECT scholarship_type, fee_waiver, COUNT(*)
FROM scholarships
GROUP BY scholarship_type, fee_waiver;

-- 4. Calculate total allocated fee waiver percentage
SELECT SUM(fee_waiver) AS total_fee_waiver_allocated FROM scholarships;
```
