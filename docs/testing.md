# System Testing & Execution Proofs
## KEC Student Sports Athletic Performance & Scholarship Evaluator

---

## 1. Test Suite Summary

This document specifies instructions and test cases for demonstrating all **13 Required Execution Proofs**:

1. Successful MySQL database connection.
2. Successful athlete registration (Phase 1).
3. Fitness score entry (Phase 1).
4. Tournament score entry (Phase 2).
5. CSV timing log import (Phase 3).
6. Regular expression validation.
7. Trial timing parsing (`MM:SS.ms`).
8. Multithreaded execution logs.
9. SPI calculation for all 3 categories.
10. Academic eligibility verification (`SGPA < 6.0`).
11. Scholarship award calculation.
12. `.txt` award letter generation.
13. Remote MySQL connection between OCI VMs.

---

## 2. Execution Proofs & Expected Outputs

### Proof 1: MySQL Database Connection
- **Test Procedure**: Run `DBConnection.testConnection()` or select Option 14 from `MainApp`.
- **Expected Console Output**:
```
[DBConnection] Loaded configuration from application.properties
[DBConnection] MySQL JDBC Driver registered successfully.
[DBConnection] Establishing new database connection to: jdbc:mysql://localhost:3306/kec_sports_db
[DBConnection] Connection test: SUCCESS
[SUCCESS] Connected to MySQL database successfully.
```

---

### Proof 2: Athlete Registration (Phase 1)
- **Test Procedure**: Select Option 1 from `MainApp` or submit Web Registration form.
- **Input Data**:
  - Student Name: `Vigneshwaran K`
  - Department: `CSE`
  - Academic Year: `3`, Semester: `5`
  - Sports Category: `INDIVIDUAL_SPORTS`, Sport: `Badminton`
  - SGPA: `8.45`
- **Expected Console Output**:
```
--- [Phase 1: Athlete Registration] ---
[SportsDAO] Generating registration ID for year 2026...
[SUCCESS] Athlete Registered Successfully!
  Athlete ID      : 7
  Registration ID : KEC-SPT-2026-007
  Eligibility     : ELIGIBLE
```

---

### Proof 3: Fitness & Endurance Score Entry (Phase 1)
- **Test Procedure**: Select Option 2 from `MainApp`.
- **Input Data**:
  - Registration ID: `KEC-SPT-2026-007`
  - Fitness Score: `88.50`
  - Endurance Score: `82.00`
- **Expected Console Output**:
```
--- [Phase 1: Record Fitness Scores] ---
Athlete: Vigneshwaran K (Badminton)
[SUCCESS] Fitness log recorded (ID: 7)
```

---

### Proof 4: Tournament Performance Entry (Phase 2)
- **Test Procedure**: Select Option 3 from `MainApp`.
- **Input Data**:
  - Registration ID: `KEC-SPT-2026-007`
  - Tournament Name: `Anna University Inter-Zonal Tournament 2026`
  - Tournament Level: `STATE`
  - Tournament Points: `85.00`
  - Achievement: `Gold Medalist`
- **Expected Console Output**:
```
--- [Phase 2: Record Tournament Performance] ---
Athlete: Vigneshwaran K | Category: INDIVIDUAL_SPORTS
[SUCCESS] Tournament score recorded successfully (ID: 7)
```

---

### Proof 5: CSV Timing Log Import (Phase 3)
- **Test Procedure**: Select Option 5 from `MainApp` or click CSV Import in Dashboard.
- **Input File**: `uploads/trial_timings.csv`
- **Expected Console Output**:
```
--- [File I/O: Import Trial Timings from CSV] ---
[CSVHandler] Import complete: 6 valid trial record(s) loaded from uploads/trial_timings.csv
Parsed 6 records from CSV.
[SUCCESS] Imported and saved 6 trial records into MySQL database.
```

---

### Proof 6: Regular Expression Validation
- **Test Procedure**: Validate Registration IDs and Timings via `StringProcessor`.
- **Test Cases & Outputs**:
```java
// Registration ID Regex: ^KEC-SPT-\d{4}-\d{3}$
StringProcessor.validateRegistrationId("KEC-SPT-2026-001"); // Returns: true
StringProcessor.validateRegistrationId("KEC-SPT-26-1");     // Returns: false (Invalid)
StringProcessor.validateRegistrationId("SPT-2026-001");     // Returns: false (Invalid)

// Trial Timing Regex: ^(\d{2}):(\d{2})\.(\d{2,3})$
StringProcessor.validateTrialTiming("00:11.25");  // Returns: true
StringProcessor.validateTrialTiming("02:15.340"); // Returns: true
StringProcessor.validateTrialTiming("11.25");     // Returns: false (Missing MM:)
```

---

### Proof 7: Trial Timing Parsing & Normalization
- **Input**: `"02:15.45"` for event with 120s benchmark.
- **Calculation**:
  - Minutes: $2 \times 60 = 120$ s
  - Seconds: $15$ s
  - Milliseconds: $45 / 100 = 0.45$ s
  - Total Actual Time: $135.45$ s
  - Benchmark Time: $120.00$ s
  - Normalized Score: $\frac{120.00}{135.45} \times 100 = 88.59\%$
- **Expected Output**:
```
Timing String  : 02:15.45
Parsed Seconds : 135.45 s
Benchmark Time : 120.00 s
Normalized Score: 88.59 / 100
```

---

### Proof 8 & 9: Multithreaded Execution Logs & SPI Calculation

- **Test Procedure**: Select Option 9 from `MainApp` or click 'Run Multithreaded Evaluation' in Dashboard.
- **Expected Console Output**:
```
========================================================
>>> STARTING MULTITHREADED EVALUATION ENGINE
>>> Total Athletes to Evaluate: 6
========================================================
[EvaluationThread-1]: Evaluating INDIVIDUAL_SPORTS for Athlete KEC-SPT-2026-001 (Arun Prakash R)
[EvaluationThread-2]: Evaluating INDIVIDUAL_SPORTS for Athlete KEC-SPT-2026-002 (Divya Shree S)
[EvaluationThread-3]: Evaluating TEAM_SPORTS for Athlete KEC-SPT-2026-003 (Karthik Venkat M)
[EvaluationThread-4]: Evaluating TEAM_SPORTS for Athlete KEC-SPT-2026-004 (Priya Dharshini K)
[EvaluationThread-1]: Evaluating TRACK_AND_FIELD for Athlete KEC-SPT-2026-005 (Suresh Babu G)
[EvaluationThread-2]: Evaluating TRACK_AND_FIELD for Athlete KEC-SPT-2026-006 (Meena Kumari T)

[EvaluationThread-1] COMPLETED KEC-SPT-2026-001 -> SPI: 81.90% | Award: SILVER | Waiver: 50.0%
[EvaluationThread-2] COMPLETED KEC-SPT-2026-002 -> SPI: 79.60% | Award: SILVER | Waiver: 50.0%
[EvaluationThread-3] COMPLETED KEC-SPT-2026-003 -> SPI: 65.60% | Award: NO_AWARD | Waiver: 0.0%
[EvaluationThread-4] COMPLETED KEC-SPT-2026-004 -> SPI: 91.50% | Award: GOLD | Waiver: 100.0%
[EvaluationThread-1] COMPLETED KEC-SPT-2026-005 -> SPI: 84.15% | Award: SILVER | Waiver: 50.0%
[EvaluationThread-2] COMPLETED KEC-SPT-2026-006 -> SPI: 59.60% | Award: NO_AWARD | Waiver: 0.0%
========================================================
>>> EVALUATION FINISHED: Processed 6 athletes.
========================================================
```

---

### Proof 10: Academic Eligibility Filter Verification (`SGPA < 6.0`)

- **Athlete 3 (`KEC-SPT-2026-003`)**: SGPA = 5.80 (< 6.0)
  - SPI = 65.60% (Reaches Bronze threshold of 60%), but:
  - Status: `INELIGIBLE`
  - Award: `NO_AWARD`
  - Fee Waiver: `0.00%`
  - Reason: `INELIGIBLE: Academic SGPA is 5.8 (< 6.0 minimum required). Scholarship revoked.`

- **Athlete 6 (`KEC-SPT-2026-006`)**: SGPA = 5.20 (< 6.0)
  - Status: `INELIGIBLE`
  - Award: `NO_AWARD`
  - Fee Waiver: `0.00%`

---

### Proof 11: Scholarship Award Calculation Verification

| Athlete | Sports Category | Component Scores | Computed SPI | Status | Award Tier | Waiver |
|:---|:---|:---|:---:|:---:|:---:|:---:|
| Priya Dharshini K | TEAM_SPORTS | Fit: 90, Tourn: 91.25, Team: 95 | **91.50%** | ELIGIBLE | **GOLD** | **100%** |
| Suresh Babu G | TRACK_AND_FIELD | Fit: 82.3, Trial: 85, Disc: 90 | **84.15%** | ELIGIBLE | **SILVER** | **50%** |
| Arun Prakash R | INDIVIDUAL_SPORTS| Fit: 85.5, Tourn: 78.5, Disc: 85| **81.90%** | ELIGIBLE | **SILVER** | **50%** |
| Divya Shree S | INDIVIDUAL_SPORTS| Fit: 72, Tourn: 82, Disc: 85 | **79.60%** | ELIGIBLE | **SILVER** | **50%** |
| Karthik Venkat M | TEAM_SPORTS | Fit: 68.75, Tourn: 55, Team: 80.5 | **65.60%** | INELIGIBLE (SGPA 5.8)| **NO_AWARD**| **0%** |
| Meena Kumari T | TRACK_AND_FIELD | Fit: 60, Trial: 58.5, Disc: 62 | **59.60%** | INELIGIBLE (SGPA 5.2)| **NO_AWARD**| **0%** |

---

### Proof 12: TXT Award Letter Generation
- **Test Procedure**: Select Option 10 or 11 from `MainApp` or click Generate in Dashboard.
- **Generated File**: `reports/AWARD_LETTER_KEC-SPT-2026-004.txt`
- **File Content**:
```
================================================================================
                        KONGU ENGINEERING COLLEGE
                          PERUNDURAI, ERODE
                                
                    SPORTS SCHOLARSHIP AWARD LETTER
================================================================================

Date: 08-Nov-2026

Athlete Name       : Priya Dharshini K
Registration ID    : KEC-SPT-2026-004
Department         : EEE
Academic Year      : Year 2
Semester           : 3
Sports Category    : TEAM_SPORTS
Sports Name        : Volleyball

--------------------------------------------------------------------------------
                         ACADEMIC ELIGIBILITY
--------------------------------------------------------------------------------
Academic SGPA      : 7.90
Eligibility Status : ELIGIBLE

--------------------------------------------------------------------------------
                       PERFORMANCE EVALUATION
--------------------------------------------------------------------------------
Fitness Score      : 90.00/100
Tournament Score   : 91.25/100
Trial Score        : N/A
Discipline Score   : 85.00/100
Teamwork Score     : 95.00/100

Sports Performance Index (SPI) : 91.50%

--------------------------------------------------------------------------------
                        SCHOLARSHIP AWARD
--------------------------------------------------------------------------------
Scholarship Category    : GOLD
Fee Waiver Percentage   : 100.00%

Congratulations! Your athletic performance has been evaluated successfully.
This scholarship has been awarded based on the institutional sports performance
and academic eligibility criteria.

================================================================================
          This is a system-generated document. No signature required.
================================================================================
```

---

### Proof 13: Remote MySQL Handshake Between OCI VMs
- **From VM 1**:
```bash
nc -zv 10.0.1.20 3306
```
- **Output**:
```
Connection to 10.0.1.20 3306 port [tcp/mysql] succeeded!
```
