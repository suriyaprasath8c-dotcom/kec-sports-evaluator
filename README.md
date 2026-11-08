# KEC Student Sports Athletic Performance & Scholarship Evaluator
### Kongu Engineering College (Autonomous) • Department of Physical Education

[![Java 17](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org/)
[![Maven 3](https://img.shields.io/badge/Build-Maven-green.svg)](https://maven.apache.org/)
[![MySQL 8](https://img.shields.io/badge/Database-MySQL%208.0-orange.svg)](https://www.mysql.com/)
[![OCI Deployment](https://img.shields.io/badge/Cloud-Oracle%20Cloud%20(OCI)-red.svg)](https://cloud.oracle.com/)

An enterprise-grade, distributed sports athletic evaluation and scholarship allocation system built with **Core Java, JDBC, Multithreading, Servlets, and MySQL 8**, deployed across a two-node **Oracle Cloud Infrastructure (OCI)** cloud architecture.

---

## 1. Team Architecture & Cloud Topology

| Role | Responsibility | Cloud Node | Key Components |
|:---|:---|:---|:---|
| **Student 1** *(Application Host)* | Application Server, Multithreading, String Parsing, File I/O, Web UI | **OCI VM 1** (Public/Private Subnet) | Apache Tomcat, Java 17, `EvaluationEngine`, `ReportExporter`, Web Servlets |
| **Student 2** *(Database Host)* | MySQL Database Administration, VCN Ingress Rules, OS Firewall Security | **OCI VM 2** (Private Subnet) | MySQL 8.0, `kec_sports_db`, `firewalld` (Port 3306 restricted to VM 1) |

---

## 2. Sports Categories & SPI Formulas

Component scores range between $0$ and $100$.

### A. Individual Sports (Badminton, Table Tennis, Tennis)
$$\text{SPI} = (\text{Fitness} \times 0.30) + (\text{Tournaments} \times 0.50) + (\text{Discipline} \times 0.20)$$
*Mandatory Threshold: Tournament Score $\ge 40.0\%$*

### B. Team Sports (Cricket, Volleyball, Football, Basketball)
$$\text{SPI} = (\text{Fitness} \times 0.40) + (\text{Tournaments} \times 0.40) + (\text{Teamwork} \times 0.20)$$
*Mandatory Threshold: Fitness Score $\ge 50.0\%$*

### C. Track & Field (100m Sprint, 400m Relay, Long Jump)
$$\text{SPI} = (\text{Fitness} \times 0.50) + (\text{Timed Trials} \times 0.40) + (\text{Discipline} \times 0.10)$$
*Mandatory Threshold: Trial Score $\ge 50.0\%$*

#### Benchmark Normalization Formula:
$$\text{Score}_{\text{trial}} = \min\left(100.0, \; \max\left(0.0, \; \frac{T_{\text{benchmark}}}{T_{\text{actual}}} \times 100\right)\right)$$

---

## 3. Academic Eligibility & Scholarship Tiers

- **Academic Eligibility Filter**: If **SGPA < 6.00**, athlete is marked **INELIGIBLE**.
  - Scholarship Award = **NO AWARD**
  - Fee Waiver = **0.00%**
  - Revocation reason recorded in database.

- **Scholarship Award Tiers**:
  | Tier | SPI Score | Institutional Fee Waiver |
  |:---|:---:|:---:|
  | 🥇 **Gold Scholarship** | 90.00% – 100.00% | **100% Waiver** |
  | 🥈 **Silver Scholarship** | 75.00% – 89.99% | **50% Waiver** |
  | 🥉 **Bronze Scholarship** | 60.00% – 74.99% | **25% Waiver** |
  | ❌ **No Award** | Below 60.00% (or Threshold failed) | **0% Waiver** |

---

## 4. Project Directory Structure

```
kec-sports-evaluator/
├── pom.xml                                   # Maven dependencies & build configuration
├── README.md                                 # Main project documentation
├── database/
│   └── kec_sports_schema.sql                 # MySQL schema, tables, indexes & sample data
├── src/main/java/com/kec/sports/
│   ├── DBConnection.java                     # Thread-safe Singleton JDBC connection manager
│   ├── MainApp.java                          # Interactive CLI application
│   ├── model/                                # POJO entities matching database tables
│   │   ├── User.java
│   │   ├── Athlete.java
│   │   ├── FitnessLog.java
│   │   ├── TournamentScore.java
│   │   ├── TrialLog.java
│   │   └── Scholarship.java
│   ├── dao/
│   │   └── SportsDAO.java                    # Complete CRUD operations with PreparedStatement
│   ├── evaluation/
│   │   ├── EvaluationTask.java               # Callable parallel evaluation worker
│   │   └── EvaluationEngine.java             # ExecutorService thread pool manager
│   ├── export/
│   │   └── ReportExporter.java               # TXT award letter & summary generator
│   ├── util/
│   │   ├── StringProcessor.java              # Regex validation, MM:SS.ms parser, sanitization
│   │   └── CSVHandler.java                   # Pure Java I/O CSV parser & exporter
│   └── servlet/                              # Web REST API controllers
│       ├── CORSFilter.java
│       ├── LoginServlet.java
│       ├── AthleteServlet.java
│       ├── FitnessServlet.java
│       ├── TournamentServlet.java
│       ├── TrialServlet.java
│       ├── EvaluationServlet.java
│       ├── ReportServlet.java
│       └── DashboardServlet.java
├── src/main/resources/
│   ├── application.properties                # Active database credentials
│   └── application.properties.example        # Configuration template
├── src/main/webapp/
│   ├── index.html                            # Navy blue themed login page
│   ├── dashboard.html                        # Comprehensive single-page dashboard
│   ├── css/style.css                         # Professional sports dashboard stylesheet
│   ├── js/app.js                             # Interactive client logic & live thread charts
│   └── WEB-INF/web.xml                       # Servlet 4.0 deployment descriptor
├── uploads/
│   └── trial_timings.csv                     # Sample trial timings CSV for batch import
├── reports/                                  # Destination for generated .txt award letters
└── docs/
    ├── architecture.md                       # OCI Cloud topology & system design
    ├── database_setup.md                     # MySQL 8 installation & privilege grants
    ├── oci_deployment.md                     # Two-VM OCI deployment instructions
    └── testing.md                            # 13 Execution proofs with expected outputs
```

---

## 5. Quickstart & Execution

### Prerequisites
- Java JDK 17 or higher
- Apache Maven 3.8+
- MySQL Server 8.0

### Step 1: Initialize Database
```bash
mysql -u root -p < database/kec_sports_schema.sql
```

### Step 2: Configure Database Credentials
Edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/kec_sports_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=kec_user
db.password=kec_sports_2026
```

### Step 3: Run Interactive CLI Demonstration
```bash
mvn compile exec:java -Dexec.mainClass="com.kec.sports.MainApp"
```

### Step 4: Build & Deploy Web Application (Tomcat)
```bash
mvn clean package
# Deploy target/kec-sports-evaluator.war to Tomcat webapps
cp target/kec-sports-evaluator.war /opt/tomcat/webapps/
```
Open browser at: `http://localhost:8080/kec-sports-evaluator/`
- Default Admin Login: `admin` / `admin123`
- Default Evaluator Login: `evaluator` / `eval123`

---

## 6. Comprehensive Documentation
- [System Architecture & Design](docs/architecture.md)
- [Database Setup & Schema Guide](docs/database_setup.md)
- [Oracle Cloud Infrastructure (OCI) Deployment](docs/oci_deployment.md)
- [Test Suite & 13 Execution Proofs](docs/testing.md)

---
*Developed for Kongu Engineering College (Autonomous), Perundurai, Erode.*
