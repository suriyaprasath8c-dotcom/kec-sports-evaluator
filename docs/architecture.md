# Architecture Documentation
## KEC Student Sports Athletic Performance & Scholarship Evaluator

---

## 1. System Overview

The **KEC Student Sports Athletic Performance & Scholarship Evaluator** is an enterprise-grade academic system developed for **Kongu Engineering College (Autonomous)** to automate athletic performance scoring, academic eligibility verification, and institutional sports scholarship fee-waiver allocations.

The system is deployed on a **Two-VM Oracle Cloud Infrastructure (OCI)** cloud architecture, strictly separating the application tier from the persistence tier across isolated private subnets.

---

## 2. Two-VM OCI Cloud Deployment Architecture

```
                  [ Student / Evaluator / Browser ]
                                  |
                                  | HTTP / HTTPS (Port 8080)
                                  v
+-------------------------------------------------------------------+
| OCI VM 1: APPLICATION HOST (Student 1)                            |
| Public IP: 140.238.xxx.xxx  |  Private IP: 10.0.1.10             |
| OS: Oracle Linux 8 / Ubuntu 22.04 LTS                             |
|                                                                   |
| +---------------------------------------------------------------+ |
| | Apache Tomcat 9 / 10 & Java 17 OpenJDK                       | |
| |                                                               | |
| | +---------------------+    +--------------------------------+ | |
| | | Web Presentation    |    | Multithreaded Evaluation Engine | | |
| | | HTML5, CSS3, JS     |--->| 4-Thread ExecutorService Pool  | | |
| | +---------------------+    +--------------------------------+ | |
| |            |                               |                  | |
| |            v                               v                  | |
| | +---------------------+    +--------------------------------+ | |
| | | REST Servlets       |    | Report Exporter                | | |
| | | Athlete, Trial, etc.|    | TXT Award Letter Generator     | | |
| | +---------------------+    +--------------------------------+ | |
| |            |                               |                  | |
| |            +---------------+---------------+                  | |
| |                            |                                  | |
| |                            v                                  | |
| |               +--------------------------+                    | |
| |               | SportsDAO (JDBC Connector)|                    | |
| |               +--------------------------+                    | |
| +----------------------------|----------------------------------+ |
+------------------------------|------------------------------------+
                               |
                               | OCI VCN Ingress Rule
                               | Private MySQL Protocol (Port 3306)
                               | Source: 10.0.1.10/32 Only
                               v
+-------------------------------------------------------------------+
| OCI VM 2: DATABASE HOST (Student 2)                               |
| Private IP: 10.0.1.20 (No Public IP / Private Subnet)             |
| OS: Oracle Linux 8 / Ubuntu 22.04 LTS                             |
|                                                                   |
| +---------------------------------------------------------------+ |
| | MySQL 8.0 Community Server (Listening on 0.0.0.0:3306)        | |
| |                                                               | |
| | Database: kec_sports_db                                       | |
| | Tables:                                                       | |
| |   • users (Admin & Evaluator Credentials)                     | |
| |   • athletes (Student Profiles & SGPA)                        | |
| |   • fitness_logs (Phase 1 Assessment)                         | |
| |   • tournament_scores (Phase 2 Achievements)                  | |
| |   • trial_logs (Phase 3 Timings & Discipline)                 | |
| |   • scholarships (SPI, Award Tiers, Fee Waivers)              | |
| |                                                               | |
| | OS Firewall: firewalld / iptables                             | |
| | Rule: ALLOW TCP 3306 from 10.0.1.10 ONLY                      | |
| +---------------------------------------------------------------+ |
+-------------------------------------------------------------------+
```

---

## 3. Layered Software Architecture

The application adopts a clean, decoupled **Model-View-Controller (MVC) + DAO** layered pattern:

1. **Presentation Layer (`src/main/webapp/`)**:
   - `index.html`: Responsive institutional login page with SHA2-256 password authentication.
   - `dashboard.html`: Single-page application (SPA) dashboard containing 10 interactive operational modules.
   - `css/style.css`: Professional sports theme utilizing navy blue (`#1a237e`), gold (`#ffd700`), white, and light grey.
   - `js/app.js`: REST consumer managing asynchronous AJAX calls, chart rendering, and live thread output.

2. **Controller / Servlet Layer (`com.kec.sports.servlet`)**:
   - `AthleteServlet.java`: Handles Phase 1 student registrations and profile lookups.
   - `FitnessServlet.java`: Records physical fitness and endurance scores.
   - `TournamentServlet.java`: Records zonal/national tournament achievements.
   - `TrialServlet.java`: Manages track & field trial timing records and multipart CSV file parsing.
   - `EvaluationServlet.java`: Dispatches multithreaded evaluation jobs and serves metric summaries.
   - `ReportServlet.java`: Streams generated `.txt` award letters and provides file download endpoints.
   - `DashboardServlet.java`: Aggregates analytics, distributions, and top performers.
   - `CORSFilter.java`: Manages cross-origin resource access.

3. **Service & Computation Layer (`com.kec.sports.evaluation`)**:
   - `EvaluationEngine.java`: Manages a dedicated `ExecutorService` thread pool to parallelize evaluations across sports disciplines concurrently.
   - `EvaluationTask.java`: Callable task executing category-specific formulas, threshold validations, and scholarship tier allocations.

4. **Data Access Layer (`com.kec.sports.dao`)**:
   - `SportsDAO.java`: Encapsulates all relational database operations using parameterized `PreparedStatement` queries and atomic database transactions.
   - `DBConnection.java`: Thread-safe Singleton connection factory reading `application.properties` with fallback to cloud environment variables.

5. **Utility Layer (`com.kec.sports.util`, `com.kec.sports.export`)**:
   - `StringProcessor.java`: Registration ID formatting (`KEC-SPT-YYYY-NNN`), regex validation, timing parser (`MM:SS.ms`), benchmark scoring, and coach remark sanitization.
   - `CSVHandler.java`: Pure Java I/O CSV parser and exporter without external library dependencies.
   - `ReportExporter.java`: Builds standardized institutional `.txt` award letters.

---

## 4. Mathematical Formulations & Category Weightings

### A. Individual Sports
- **Fitness Test**: 30%
- **Tournaments**: 50%
- **Discipline Metric**: 20%
- **Mandatory Threshold**: Tournament Score $\ge 40.0\%$
$$\text{SPI} = (\text{Fitness} \times 0.30) + (\text{Tournament} \times 0.50) + (\text{Discipline} \times 0.20)$$

### B. Team Sports
- **Fitness Test**: 40%
- **Tournaments**: 40%
- **Teamwork Score**: 20%
- **Mandatory Threshold**: Fitness Score $\ge 50.0\%$
$$\text{SPI} = (\text{Fitness} \times 0.40) + (\text{Tournament} \times 0.40) + (\text{Teamwork} \times 0.20)$$

### C. Track & Field
- **Fitness Test**: 50%
- **Timed Trial Score**: 40%
- **Discipline Metric**: 10%
- **Mandatory Threshold**: Trial Score $\ge 50.0\%$
$$\text{SPI} = (\text{Fitness} \times 0.50) + (\text{Trial Score} \times 0.40) + (\text{Discipline} \times 0.10)$$

#### Trial Timing Normalization Formula:
Timed trials in `MM:SS.ms` are parsed to total seconds $T_{\text{actual}}$. A lower time indicates superior performance:
$$\text{Score}_{\text{trial}} = \min\left(100.0, \; \max\left(0.0, \; \frac{T_{\text{benchmark}}}{T_{\text{actual}}} \times 100\right)\right)$$
*Configurable event benchmarks:*
- 100m Sprint: 11.00s
- 200m Sprint: 22.50s
- 400m Sprint: 50.00s
- 400m Relay: 45.00s

---

## 5. Academic Filter & Scholarship Tiers

1. **Academic Eligibility Filter**:
   $$\text{Eligibility} = \begin{cases} \text{ELIGIBLE}, & \text{SGPA} \ge 6.00 \\ \text{INELIGIBLE}, & \text{SGPA} < 6.00 \end{cases}$$
   *If INELIGIBLE: Scholarship revoked, Fee Waiver = 0.0%, Status marked INELIGIBLE.*

2. **Scholarship Award Matrix**:
   | Scholarship Tier | Required SPI | Fee Waiver | Conditions |
   |:---|:---:|:---:|:---|
   | **Gold Scholarship** | 90.00% – 100.00% | **100% Waiver** | Category Threshold Met & SGPA $\ge 6.0$ |
   | **Silver Scholarship** | 75.00% – 89.99% | **50% Waiver** | Category Threshold Met & SGPA $\ge 6.0$ |
   | **Bronze Scholarship** | 60.00% – 74.99% | **25% Waiver** | Category Threshold Met & SGPA $\ge 6.0$ |
   | **No Award** | Below 60.00% | **0% Waiver** | Minimum score not met or threshold failed |

---

## 6. Multithreading Design

Concurrency is managed through `java.util.concurrent.ExecutorService`:
- Tasks are submitted as `Callable<Scholarship>`.
- Thread naming uses a custom `ThreadFactory` (`EvaluationThread-1`, `EvaluationThread-2`, etc.).
- Safe result harvesting uses `Future.get(10, TimeUnit.SECONDS)`.
- Isolation ensures an exception in evaluating one athlete does not terminate the engine or affect peer threads.
