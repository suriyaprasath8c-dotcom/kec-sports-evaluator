# KEC Student Sports Athletic Performance & Scholarship Evaluator
### Kongu Engineering College (Autonomous), Perundurai - 638060
**Department of Computer Science and Engineering / Department of Physical Education**

- **Project Title**: KEC Student Sports Athletic Performance & Scholarship Evaluator
- **Assignment Set**: Set 7
- **Academic Year**: 2025 - 2026
- **Architecture**: Distributed 2-Node Cloud Deployment (Oracle Cloud Infrastructure - OCI)

#### Team Members & Cloud Architecture:
- **Student 1 (Application Host - OCI VM 1)**: Vaisnav K (Reg No: 22CSR180)
  - Responsibilities: Application hosting, Java multithreaded evaluation engine (`ExecutorService`), String regex validation, trial timing conversion (`MM:SS.ms`), raw Java CSV parsing, and `.txt` award letter generation.
- **Student 2 (Database Host - OCI VM 2)**: Database Admin Partner (Reg No: 22CSR181)
  - Responsibilities: MySQL 8 database administration, OCI VCN Ingress Rules (port 3306 restricted to VM 1), OS firewall (`firewalld`), and schema management (`kec_sports_schema.sql`).

---

## 1. Project Overview

At Kongu Engineering College, student-athletes represent the institution across zonal, inter-collegiate, state, and national tournaments. This system automates the multi-phase athletic evaluation and determines institutional tuition fee waiver scholarships based on athletic performance and academic eligibility.

### Three Sports Categories & SPI Formulas:
Component scores range from 0 to 100.

1. **Individual Sports (Badminton, Table Tennis, Tennis)**:
   - Fitness Test: 30% | Tournaments: 50% | Discipline: 20%
   - *Minimum Tournament Score Threshold*: 40%
   - $$\text{SPI} = (\text{Fitness} \times 0.30) + (\text{Tournaments} \times 0.50) + (\text{Discipline} \times 0.20)$$

2. **Team Sports (Cricket, Volleyball, Football, Basketball)**:
   - Fitness Test: 40% | Tournaments: 40% | Teamwork: 20%
   - *Minimum Fitness Score Threshold*: 50%
   - $$\text{SPI} = (\text{Fitness} \times 0.40) + (\text{Tournaments} \times 0.40) + (\text{Teamwork} \times 0.20)$$

3. **Track and Field (100m, 200m, 400m Relay, Long Jump, High Jump)**:
   - Fitness Test: 50% | Timed Trials: 40% | Discipline: 10%
   - *Minimum Trial Score Threshold*: 50%
   - $$\text{SPI} = (\text{Fitness} \times 0.50) + (\text{Trial Score} \times 0.40) + (\text{Discipline} \times 0.10)$$
   - *Timing Normalization Formula*: Given trial timing in `MM:SS.ms`, actual seconds $T_{\text{actual}}$ are calculated. Faster times give higher scores:
     $$\text{Score} = \min\left(100.0, \; \max\left(0.0, \; \frac{T_{\text{benchmark}}}{T_{\text{actual}}} \times 100\right)\right)$$

### Academic Eligibility Filter:
- According to college policy, students must maintain a minimum **SGPA $\ge$ 6.00** to receive sports scholarships.
- If **SGPA < 6.00**: marked **INELIGIBLE**, scholarship is revoked (**0% Fee Waiver**, Award: `NO_AWARD`).

### Scholarship Award Tiers:
| Scholarship Tier | Required SPI | Institutional Fee Waiver |
|:---|:---:|:---:|
| 🥇 **Gold Scholarship** | 90.00% – 100.00% | **100% Tuition Fee Waiver** |
| 🥈 **Silver Scholarship** | 75.00% – 89.99% | **50% Tuition Fee Waiver** |
| 🥉 **Bronze Scholarship** | 60.00% – 74.99% | **25% Tuition Fee Waiver** |
| ❌ **No Award** | Below 60.00% | **0% Waiver** |

---

## 2. Project Directory Structure

```
D:\kec-sports-evaluator\
├── build.bat                                 # Windows batch script to package WAR
├── run_cli.bat                               # Windows batch script to run in CMD directly
├── pom.xml                                   # Maven configuration file
├── README.md                                 # Project documentation
├── database/
│   └── kec_sports_schema.sql                 # MySQL 8 database schema & sample data
├── lib/
│   └── mysql-connector-j.jar                 # Standalone MySQL JDBC driver for CMD execution
├── src/main/java/com/kec/sports/
│   ├── DBConnection.java                     # Singleton JDBC connection manager
│   ├── MainApp.java                          # Interactive console application
│   ├── dao/
│   │   └── SportsDAO.java                    # PreparedStatement CRUD operations
│   ├── evaluation/
│   │   ├── EvaluationEngine.java             # Multithreaded ExecutorService coordinator
│   │   └── EvaluationTask.java               # Callable worker executing SPI formulas
│   ├── export/
│   │   └── ReportExporter.java               # Generates TXT award letters
│   ├── model/
│   │   ├── Athlete.java
│   │   ├── FitnessLog.java
│   │   ├── TournamentScore.java
│   │   ├── TrialLog.java
│   │   ├── Scholarship.java
│   │   └── User.java
│   ├── servlet/                              # Web REST API controllers
│   │   ├── AthleteServlet.java
│   │   ├── CORSFilter.java
│   │   ├── DashboardServlet.java
│   │   ├── EvaluationServlet.java
│   │   ├── FitnessServlet.java
│   │   ├── LoginServlet.java
│   │   ├── ReportServlet.java
│   │   ├── TournamentServlet.java
│   │   └── TrialServlet.java
│   └── util/
│       ├── CSVHandler.java                   # Raw Java I/O CSV parser & exporter
│       └── StringProcessor.java              # Regex checks, MM:SS.ms timing parser
├── src/main/resources/
│   ├── application.properties                # Database connection settings
│   └── application.properties.example        # Reference configuration
├── src/main/webapp/
│   ├── index.html                            # Login page
│   ├── dashboard.html                        # Modern responsive dashboard
│   ├── css/style.css                         # Navy blue sports portal styling
│   ├── js/app.js                             # Client-side logic & charts
│   └── WEB-INF/web.xml                       # Servlet descriptor
├── uploads/
│   └── trial_timings.csv                     # Sample trial timings CSV
├── reports/                                  # Folder where .txt award letters are generated
└── docs/
    ├── architecture.md                       # OCI Cloud architecture details
    ├── database_setup.md                     # MySQL 8 setup & permissions
    ├── oci_deployment.md                     # 2-VM deployment steps & firewall commands
    └── testing.md                            # 13 Execution proofs with sample runs
```

---

## 3. How to Run in Command Prompt (CMD)

You can run the application directly on Windows without complicated setup:

### Method 1: Using `run_cli.bat` (Recommended)
Open CMD and run:
```cmd
cd /d D:\kec-sports-evaluator
run_cli.bat
```
*(Or double-click `run_cli.bat` in File Explorer).*

### Method 2: Direct Java Command in CMD
```cmd
cd /d D:\kec-sports-evaluator
javac -encoding UTF-8 -cp "lib\*" -d bin src\main\java\com\kec\sports\model\*.java src\main\java\com\kec\sports\util\*.java src\main\java\com\kec\sports\dao\*.java src\main\java\com\kec\sports\export\*.java src\main\java\com\kec\sports\evaluation\*.java src\main\java\com\kec\sports\*.java
java -cp "bin;lib\*;src\main\resources" com.kec.sports.MainApp
```

### Method 3: Using Apache Maven
```cmd
cd /d D:\kec-sports-evaluator
mvn compile exec:java -Dexec.mainClass="com.kec.sports.MainApp"
```

---

## 4. How to Deploy on Tomcat (Web Dashboard)

1. Build the WAR package:
   ```cmd
   cd /d D:\kec-sports-evaluator
   build.bat
   ```
2. Copy `target\kec-sports-evaluator.war` into Tomcat's `webapps/` folder.
3. Start Tomcat and visit:
   ```
   http://localhost:8080/kec-sports-evaluator/
   ```
   - Default login: `admin` / `admin123`

---

## 5. Pushing to GitHub (`vaisnav7/kec-sports-evaluator`)

The local repository is initialized and committed on `main`. To push to your GitHub:
1. Create a repository on GitHub named `kec-sports-evaluator` at: **https://github.com/new**
2. In CMD, run:
```cmd
cd /d D:\kec-sports-evaluator
git push -u origin main
```
