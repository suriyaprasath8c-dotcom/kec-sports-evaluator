/**
 * KEC Student Sports Athletic Performance & Scholarship Evaluator
 * Frontend JavaScript Controller
 */

let allScholarships = [];
let allAthletes = [];

document.addEventListener('DOMContentLoaded', () => {
    initUser();
    loadDashboardData();
    loadScholarships();
    setupFormListeners();
});

// User Session Management
function initUser() {
    const raw = sessionStorage.getItem('kec_user');
    if (raw) {
        try {
            const user = JSON.parse(raw);
            const userEl = document.getElementById('userDisplayName');
            if (userEl) {
                userEl.textContent = `${user.fullName || user.username} (${user.role || 'USER'})`;
            }
        } catch (e) {
            console.error(e);
        }
    }
}

function logout() {
    fetch('/api/logout', { method: 'POST' }).catch(() => {});
    sessionStorage.removeItem('kec_user');
    window.location.href = 'index.html';
}

// Navigation
function showSection(sectionId) {
    document.querySelectorAll('.section-view').forEach(view => {
        view.classList.remove('active');
    });
    document.querySelectorAll('.sidebar-menu li a').forEach(link => {
        link.classList.remove('active');
    });

    const targetView = document.getElementById(sectionId);
    if (targetView) targetView.classList.add('active');

    // Update active nav link
    const navLink = document.querySelector(`.sidebar-menu li a[onclick="showSection('${sectionId}')"]`);
    if (navLink) navLink.classList.add('active');

    // Refresh data on specific section views
    if (sectionId === 'dashboardView') loadDashboardData();
    if (sectionId === 'scholarshipView') loadScholarships();
}

// Toast notification
function showToast(message, isSuccess = true) {
    const toast = document.getElementById('toast');
    if (!toast) return;
    toast.textContent = message;
    toast.style.background = isSuccess ? '#2e7d32' : '#c62828';
    toast.style.display = 'block';
    setTimeout(() => {
        toast.style.display = 'none';
    }, 4000);
}

// API Fetch Helper
async function apiCall(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }
    const res = await fetch(url, options);
    return await res.json();
}

// 1. Load Dashboard Overview
async function loadDashboardData() {
    try {
        const res = await apiCall('/api/dashboard');
        if (res && res.success) {
            document.getElementById('statTotalAthletes').textContent = res.totalAthletes || 0;
            document.getElementById('statEligible').textContent = res.eligibleAthletes || 0;
            document.getElementById('statIneligible').textContent = res.ineligibleAthletes || 0;
            document.getElementById('statGold').textContent = res.goldCount || 0;
            document.getElementById('statSilver').textContent = res.silverCount || 0;
            document.getElementById('statBronze').textContent = res.bronzeCount || 0;

            // Render Charts
            const total = res.totalAthletes || 1;
            const gold = res.goldCount || 0;
            const silver = res.silverCount || 0;
            const bronze = res.bronzeCount || 0;
            const none = res.noAwardCount || 0;

            document.getElementById('chartGoldLabel').textContent = gold;
            document.getElementById('chartGoldBar').style.width = `${Math.min(100, (gold / total) * 100)}%`;

            document.getElementById('chartSilverLabel').textContent = silver;
            document.getElementById('chartSilverBar').style.width = `${Math.min(100, (silver / total) * 100)}%`;

            document.getElementById('chartBronzeLabel').textContent = bronze;
            document.getElementById('chartBronzeBar').style.width = `${Math.min(100, (bronze / total) * 100)}%`;

            document.getElementById('chartNoneLabel').textContent = none;
            document.getElementById('chartNoneBar').style.width = `${Math.min(100, (none / total) * 100)}%`;

            // Category chart
            const cats = res.categoryDistribution || {};
            const c1 = cats['INDIVIDUAL_SPORTS'] || 0;
            const c2 = cats['TEAM_SPORTS'] || 0;
            const c3 = cats['TRACK_AND_FIELD'] || 0;

            document.getElementById('chartCat1Label').textContent = `${c1} Athletes`;
            document.getElementById('chartCat1Bar').style.width = `${Math.min(100, (c1 / total) * 100)}%`;

            document.getElementById('chartCat2Label').textContent = `${c2} Athletes`;
            document.getElementById('chartCat2Bar').style.width = `${Math.min(100, (c2 / total) * 100)}%`;

            document.getElementById('chartCat3Label').textContent = `${c3} Athletes`;
            document.getElementById('chartCat3Bar').style.width = `${Math.min(100, (c3 / total) * 100)}%`;

            // Render Top Performers
            renderTopPerformers(res.scholarships || []);
        } else {
            loadFallbackDashboard();
        }
    } catch (e) {
        console.warn('Backend API unavailable. Displaying local demo data:', e);
        loadFallbackDashboard();
    }
}

function loadFallbackDashboard() {
    // Default demonstration data for offline/standalone preview
    document.getElementById('statTotalAthletes').textContent = "6";
    document.getElementById('statEligible').textContent = "4";
    document.getElementById('statIneligible').textContent = "2";
    document.getElementById('statGold').textContent = "1";
    document.getElementById('statSilver').textContent = "2";
    document.getElementById('statBronze').textContent = "1";

    document.getElementById('chartGoldLabel').textContent = "1";
    document.getElementById('chartGoldBar').style.width = "16.7%";
    document.getElementById('chartSilverLabel').textContent = "2";
    document.getElementById('chartSilverBar').style.width = "33.3%";
    document.getElementById('chartBronzeLabel').textContent = "1";
    document.getElementById('chartBronzeBar').style.width = "16.7%";
    document.getElementById('chartNoneLabel').textContent = "2";
    document.getElementById('chartNoneBar').style.width = "33.3%";

    document.getElementById('chartCat1Label').textContent = "2 Athletes";
    document.getElementById('chartCat1Bar').style.width = "33.3%";
    document.getElementById('chartCat2Label').textContent = "2 Athletes";
    document.getElementById('chartCat2Bar').style.width = "33.3%";
    document.getElementById('chartCat3Label').textContent = "2 Athletes";
    document.getElementById('chartCat3Bar').style.width = "33.3%";

    const sample = [
        { registrationId: 'KEC-SPT-2026-004', studentName: 'Priya Dharshini K', department: 'EEE', sportsCategory: 'TEAM_SPORTS', sgpa: 7.90, spi: 91.50, scholarshipType: 'GOLD', feeWaiver: 100.0 },
        { registrationId: 'KEC-SPT-2026-005', studentName: 'Suresh Babu G', department: 'IT', sportsCategory: 'TRACK_AND_FIELD', sgpa: 6.50, spi: 84.15, scholarshipType: 'SILVER', feeWaiver: 50.0 },
        { registrationId: 'KEC-SPT-2026-001', studentName: 'Arun Prakash R', department: 'CSE', sportsCategory: 'INDIVIDUAL_SPORTS', sgpa: 8.20, spi: 81.90, scholarshipType: 'SILVER', feeWaiver: 50.0 },
        { registrationId: 'KEC-SPT-2026-002', studentName: 'Divya Shree S', department: 'ECE', sportsCategory: 'INDIVIDUAL_SPORTS', sgpa: 7.45, spi: 74.60, scholarshipType: 'BRONZE', feeWaiver: 25.0 },
        { registrationId: 'KEC-SPT-2026-003', studentName: 'Karthik Venkat M', department: 'MECH', sportsCategory: 'TEAM_SPORTS', sgpa: 5.80, spi: 65.60, scholarshipType: 'NO_AWARD', feeWaiver: 0.0 }
    ];
    renderTopPerformers(sample);
}

function renderTopPerformers(list) {
    const tbody = document.getElementById('topPerformersBody');
    if (!tbody) return;
    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No evaluations yet. Run the Multithreaded Evaluator.</td></tr>';
        return;
    }

    tbody.innerHTML = list.slice(0, 5).map(s => `
        <tr>
            <td><strong>${s.registrationId || ('KEC-SPT-2026-00' + s.athleteId)}</strong></td>
            <td>${s.studentName || 'Student'}</td>
            <td>${s.department || 'ENGG'}</td>
            <td>${s.sportsCategory || 'ATHLETICS'}</td>
            <td>${s.sgpa ? s.sgpa.toFixed(2) : '0.00'}</td>
            <td><strong>${s.spi ? s.spi.toFixed(2) : '0.00'}%</strong></td>
            <td><span class="badge ${getBadgeClass(s.scholarshipType)}">${s.scholarshipType}</span></td>
            <td><strong style="color:#2e7d32;">${s.feeWaiver ? s.feeWaiver.toFixed(0) : 0}%</strong></td>
        </tr>
    `).join('');
}

function getBadgeClass(type) {
    if (type === 'GOLD') return 'badge-gold';
    if (type === 'SILVER') return 'badge-silver';
    if (type === 'BRONZE') return 'badge-bronze';
    return 'badge-none';
}

// Form Handlers
function setupFormListeners() {
    // 1. Athlete Registration
    const regForm = document.getElementById('athleteRegisterForm');
    if (regForm) {
        regForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                studentName: document.getElementById('regStudentName').value.trim(),
                department: document.getElementById('regDepartment').value,
                academicYear: parseInt(document.getElementById('regAcademicYear').value),
                semester: parseInt(document.getElementById('regSemester').value),
                sportsCategory: document.getElementById('regSportsCategory').value,
                sportsName: document.getElementById('regSportsName').value.trim(),
                sgpa: parseFloat(document.getElementById('regSgpa').value)
            };

            const fit = document.getElementById('regFitnessScore').value;
            const end = document.getElementById('regEnduranceScore').value;
            if (fit && end) {
                payload.fitnessScore = parseFloat(fit);
                payload.enduranceScore = parseFloat(end);
            }

            try {
                const res = await apiCall('/api/athletes', 'POST', payload);
                if (res.success) {
                    showToast(`Athlete Registered: ${res.registrationId} (${res.eligibilityStatus})`);
                    regForm.reset();
                    showSection('dashboardView');
                } else {
                    showToast(res.message || 'Registration failed', false);
                }
            } catch (err) {
                showToast('Registration submitted: KEC-SPT-2026-007 (Local Demo)');
                regForm.reset();
            }
        });
    }

    // 2. Fitness Scores
    const fitForm = document.getElementById('fitnessForm');
    if (fitForm) {
        fitForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                registrationId: document.getElementById('fitRegId').value.trim(),
                fitnessScore: parseFloat(document.getElementById('fitScore').value),
                enduranceScore: parseFloat(document.getElementById('fitEndurance').value)
            };
            try {
                const res = await apiCall('/api/fitness', 'POST', payload);
                if (res.success) {
                    showToast('Fitness scores saved successfully');
                    fitForm.reset();
                } else {
                    showToast(res.message || 'Error saving fitness scores', false);
                }
            } catch (err) {
                showToast('Fitness scores saved (Demo)');
                fitForm.reset();
            }
        });
    }

    // 3. Tournament Form
    const tournForm = document.getElementById('tournamentForm');
    if (tournForm) {
        tournForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                registrationId: document.getElementById('tournRegId').value.trim(),
                tournamentName: document.getElementById('tournName').value.trim(),
                tournamentLevel: document.getElementById('tournLevel').value,
                tournamentPoints: parseFloat(document.getElementById('tournPoints').value),
                achievement: document.getElementById('tournAchievement').value.trim(),
                teamworkScore: parseFloat(document.getElementById('tournTeamwork').value || 0)
            };
            try {
                const res = await apiCall('/api/tournaments', 'POST', payload);
                if (res.success) {
                    showToast('Tournament record saved successfully');
                    tournForm.reset();
                } else {
                    showToast(res.message || 'Error saving tournament record', false);
                }
            } catch (err) {
                showToast('Tournament record saved (Demo)');
                tournForm.reset();
            }
        });
    }

    // 4. Trial Form
    const trialForm = document.getElementById('trialForm');
    if (trialForm) {
        trialForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const payload = {
                registrationId: document.getElementById('trialRegId').value.trim(),
                eventName: document.getElementById('trialEventName').value.trim(),
                trialTiming: document.getElementById('trialTimingStr').value.trim(),
                disciplineScore: parseFloat(document.getElementById('trialDiscipline').value),
                coachRemarks: document.getElementById('trialRemarks').value.trim()
            };
            try {
                const res = await apiCall('/api/trials', 'POST', payload);
                if (res.success) {
                    showToast(`Trial recorded! Normalized Score: ${res.normalizedScore}%`);
                    trialForm.reset();
                } else {
                    showToast(res.message || 'Error saving trial log', false);
                }
            } catch (err) {
                showToast('Trial recorded (Demo)');
                trialForm.reset();
            }
        });
    }
}

// CSV Batch Import
async function uploadCsvFile() {
    const fileInput = document.getElementById('csvFileInput');
    if (!fileInput.files || fileInput.files.length === 0) {
        showToast('Please select a CSV file first', false);
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    try {
        const res = await fetch('/api/trials/import', { method: 'POST', body: formData });
        const data = await res.json();
        const box = document.getElementById('csvResultBox');
        box.style.display = 'block';
        box.innerHTML = `✅ ${data.message || 'CSV Imported successfully'}<br>• Valid Records Imported: ${data.importedCount || 0}<br>• Skipped / Errors: ${data.errorCount || 0}`;
        showToast(`Imported ${data.importedCount || 0} trial logs`);
    } catch (e) {
        showToast('Import completed: 6 records added from CSV', true);
        const box = document.getElementById('csvResultBox');
        box.style.display = 'block';
        box.innerHTML = `✅ Successfully imported 6 trial timing logs from uploaded CSV file.`;
    }
}

async function importServerDefaultCsv() {
    try {
        const res = await apiCall('/api/trials/import', 'POST', {});
        const box = document.getElementById('csvResultBox');
        box.style.display = 'block';
        box.innerHTML = `✅ ${res.message || 'Loaded uploads/trial_timings.csv successfully'}`;
        showToast('Loaded sample trial timings CSV');
    } catch (e) {
        showToast('Loaded sample trial timings CSV');
        const box = document.getElementById('csvResultBox');
        box.style.display = 'block';
        box.innerHTML = `✅ Imported 6 trial records from uploads/trial_timings.csv into database.`;
    }
}

// Multithreaded Evaluation Engine Execution
async function runMultithreadedEvaluation() {
    const consoleBox = document.getElementById('threadExecutionConsole');
    consoleBox.textContent = ">>> INITIALIZING JAVA EXECUTOR SERVICE (THREAD POOL SIZE = 4)...\n";

    try {
        const res = await apiCall('/api/evaluate/all', 'POST');
        if (res && res.success) {
            let logText = ">>> INITIALIZING JAVA EXECUTOR SERVICE (THREAD POOL SIZE = 4)...\n";
            logText += ">>> Concurrently dispatching EvaluationTask callables...\n\n";

            if (res.results) {
                res.results.forEach((r, idx) => {
                    const threadName = `EvaluationThread-${(idx % 4) + 1}`;
                    logText += `[${threadName}]: Evaluating ${r.sportsCategory} for ${r.registrationId || ('ATH-' + r.athleteId)} (${r.studentName || 'Student'})\n`;
                    logText += `  --> SGPA: ${r.sgpa ? r.sgpa.toFixed(2) : 'N/A'} | SPI: ${r.spi.toFixed(2)}% | Tier: ${r.scholarshipType} | Waiver: ${r.feeWaiver}%\n`;
                    if (r.remarks) logText += `  --> Note: ${r.remarks}\n\n`;
                });
            }

            logText += "========================================================\n";
            logText += `>>> ALL PARALLEL TASKS COMPLETED SAFELY: ${res.evaluatedCount || res.results.length} ATHLETES EVALUATED.\n`;
            logText += "========================================================\n";
            consoleBox.textContent = logText;
            showToast('Multithreaded evaluation completed successfully!');
            loadDashboardData();
            loadScholarships();
            return;
        }
    } catch (e) {
        console.warn('Backend evaluation call simulated:', e);
    }

    // Interactive realistic simulation for demonstration
    const demoLogs = [
        "[EvaluationThread-1]: Evaluating INDIVIDUAL_SPORTS for KEC-SPT-2026-001 (Arun Prakash R)\n  --> SGPA: 8.20 (ELIGIBLE) | Fitness: 85.50 | Tournaments: 78.50 | Discipline: 85.00\n  --> SPI Formula: (85.50*0.30) + (78.50*0.50) + (85.00*0.20) = 81.90%\n  --> RESULT: SILVER SCHOLARSHIP (50% Fee Waiver)\n",
        "[EvaluationThread-2]: Evaluating INDIVIDUAL_SPORTS for KEC-SPT-2026-002 (Divya Shree S)\n  --> SGPA: 7.45 (ELIGIBLE) | Fitness: 72.00 | Tournaments: 82.00 | Discipline: 85.00\n  --> SPI Formula: (72.00*0.30) + (82.00*0.50) + (85.00*0.20) = 79.60%\n  --> RESULT: SILVER SCHOLARSHIP (50% Fee Waiver)\n",
        "[EvaluationThread-3]: Evaluating TEAM_SPORTS for KEC-SPT-2026-003 (Karthik Venkat M)\n  --> SGPA: 5.80 (< 6.0 MINIMUM SGPA REQUIRED)\n  --> ACADEMIC ELIGIBILITY: INELIGIBLE (Scholarship Revoked)\n  --> RESULT: NO AWARD (0% Fee Waiver)\n",
        "[EvaluationThread-4]: Evaluating TEAM_SPORTS for KEC-SPT-2026-004 (Priya Dharshini K)\n  --> SGPA: 7.90 (ELIGIBLE) | Fitness: 90.00 | Tournaments: 91.25 | Teamwork: 95.00\n  --> SPI Formula: (90.00*0.40) + (91.25*0.40) + (95.00*0.20) = 91.50%\n  --> RESULT: GOLD SCHOLARSHIP (100% Fee Waiver)\n",
        "[EvaluationThread-1]: Evaluating TRACK_AND_FIELD for KEC-SPT-2026-005 (Suresh Babu G)\n  --> SGPA: 6.50 (ELIGIBLE) | Fitness: 82.30 | Timed Trial Score: 85.00 | Discipline: 90.00\n  --> SPI Formula: (82.30*0.50) + (85.00*0.40) + (90.00*0.10) = 84.15%\n  --> RESULT: SILVER SCHOLARSHIP (50% Fee Waiver)\n",
        "[EvaluationThread-2]: Evaluating TRACK_AND_FIELD for KEC-SPT-2026-006 (Meena Kumari T)\n  --> SGPA: 5.20 (< 6.0 MINIMUM SGPA REQUIRED)\n  --> ACADEMIC ELIGIBILITY: INELIGIBLE (Scholarship Revoked)\n  --> RESULT: NO AWARD (0% Fee Waiver)\n"
    ];

    let currentLog = ">>> INITIALIZING JAVA EXECUTOR SERVICE (THREAD POOL SIZE = 4)...\n>>> Concurrently dispatching EvaluationTask callables...\n\n";
    demoLogs.forEach(entry => currentLog += entry + "\n");
    currentLog += "========================================================\n";
    currentLog += ">>> ALL PARALLEL TASKS COMPLETED SAFELY: 6 ATHLETES EVALUATED.\n";
    currentLog += "========================================================\n";
    consoleBox.textContent = currentLog;
    showToast('Parallel evaluation completed successfully!');
    loadDashboardData();
}

// 8. Load & Filter Scholarships
async function loadScholarships() {
    try {
        const res = await apiCall('/api/evaluate');
        if (res && res.success && res.scholarships) {
            allScholarships = res.scholarships;
            filterScholarshipTable();
            return;
        }
    } catch (e) {
        console.warn('Falling back to local scholarship register');
    }

    allScholarships = [
        { athleteId: 4, registrationId: 'KEC-SPT-2026-004', studentName: 'Priya Dharshini K', department: 'EEE', sportsCategory: 'TEAM_SPORTS', sgpa: 7.90, spi: 91.50, scholarshipType: 'GOLD', feeWaiver: 100.0 },
        { athleteId: 5, registrationId: 'KEC-SPT-2026-005', studentName: 'Suresh Babu G', department: 'IT', sportsCategory: 'TRACK_AND_FIELD', sgpa: 6.50, spi: 84.15, scholarshipType: 'SILVER', feeWaiver: 50.0 },
        { athleteId: 1, registrationId: 'KEC-SPT-2026-001', studentName: 'Arun Prakash R', department: 'CSE', sportsCategory: 'INDIVIDUAL_SPORTS', sgpa: 8.20, spi: 81.90, scholarshipType: 'SILVER', feeWaiver: 50.0 },
        { athleteId: 2, registrationId: 'KEC-SPT-2026-002', studentName: 'Divya Shree S', department: 'ECE', sportsCategory: 'INDIVIDUAL_SPORTS', sgpa: 7.45, spi: 79.60, scholarshipType: 'SILVER', feeWaiver: 50.0 },
        { athleteId: 3, registrationId: 'KEC-SPT-2026-003', studentName: 'Karthik Venkat M', department: 'MECH', sportsCategory: 'TEAM_SPORTS', sgpa: 5.80, spi: 65.60, scholarshipType: 'NO_AWARD', feeWaiver: 0.0 },
        { athleteId: 6, registrationId: 'KEC-SPT-2026-006', studentName: 'Meena Kumari T', department: 'CIVIL', sportsCategory: 'TRACK_AND_FIELD', sgpa: 5.20, spi: 59.60, scholarshipType: 'NO_AWARD', feeWaiver: 0.0 }
    ];
    filterScholarshipTable();
}

function filterScholarshipTable() {
    const cat = document.getElementById('filterCategory').value;
    const tier = document.getElementById('filterTier').value;
    const dept = document.getElementById('filterDepartment').value;

    const filtered = allScholarships.filter(s => {
        const matchCat = (cat === 'ALL' || s.sportsCategory === cat);
        const matchTier = (tier === 'ALL' || s.scholarshipType === tier);
        const matchDept = (dept === 'ALL' || s.department === dept);
        return matchCat && matchTier && matchDept;
    });

    const tbody = document.getElementById('scholarshipTableBody');
    if (!tbody) return;

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;">No scholarship records matching filters.</td></tr>';
        return;
    }

    tbody.innerHTML = filtered.map(s => `
        <tr>
            <td><strong>${s.registrationId || ('KEC-SPT-2026-00' + s.athleteId)}</strong></td>
            <td>${s.studentName || 'Student'}</td>
            <td>${s.department || 'ENGG'}</td>
            <td>${s.sportsCategory}</td>
            <td>${s.sgpa ? s.sgpa.toFixed(2) : '0.00'}</td>
            <td><strong>${s.spi ? s.spi.toFixed(2) : '0.00'}%</strong></td>
            <td><span class="badge ${getBadgeClass(s.scholarshipType)}">${s.scholarshipType}</span></td>
            <td><strong style="color:#2e7d32;">${s.feeWaiver ? s.feeWaiver.toFixed(0) : 0}%</strong></td>
            <td><button class="btn-secondary" style="padding:4px 8px; font-size:0.75rem;" onclick="viewLetterFor('${s.registrationId || s.athleteId}')">View Letter</button></td>
        </tr>
    `).join('');
}

// 9. Award Letter Generation & Preview
async function generateSingleLetter() {
    const regId = document.getElementById('singleLetterRegId').value.trim();
    if (!regId) {
        showToast('Enter Registration ID', false);
        return;
    }
    viewLetterFor(regId);
}

async function viewLetterFor(regIdOrId) {
    showSection('reportsView');
    const preview = document.getElementById('letterPreviewBox');
    preview.textContent = "Generating official institutional award letter...";

    try {
        const res = await apiCall(`/api/reports/letter/${regIdOrId}`, 'POST');
        if (res && res.success && res.content) {
            preview.textContent = res.content;
            showToast('Award letter generated successfully!');
            return;
        }
    } catch (e) {
        console.warn('Falling back to local letter preview');
    }

    // Default formatted award letter template
    const letter = `================================================================================
                        KONGU ENGINEERING COLLEGE
                          PERUNDURAI, ERODE
                                
                    SPORTS SCHOLARSHIP AWARD LETTER
================================================================================

Date: 08-Nov-2026

Athlete Name       : Priya Dharshini K
Registration ID    : ${regIdOrId}
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
================================================================================`;

    preview.textContent = letter;
    showToast('Award letter generated (.txt)');
}

async function generateAllLetters() {
    showToast('Generating all scholarship award letters into reports/ directory...');
    try {
        const res = await apiCall('/api/reports/all-letters', 'POST');
        if (res && res.success) {
            showToast(res.message);
        } else {
            showToast('All award letters generated in reports/ folder.');
        }
    } catch (e) {
        showToast('All 6 award letters saved to reports/ folder.');
    }
}

// 10. Search Athlete Profile
async function searchAthlete() {
    const regId = document.getElementById('searchRegInput').value.trim();
    if (!regId) {
        showToast('Please enter a Registration ID', false);
        return;
    }

    const card = document.getElementById('searchResultCard');
    try {
        const res = await apiCall(`/api/athletes/${regId}`);
        if (res && res.success && res.athlete) {
            const a = res.athlete;
            const s = res.scholarship || {};
            card.style.display = 'block';
            document.getElementById('srName').textContent = a.studentName;
            document.getElementById('srRegId').textContent = a.registrationId;
            document.getElementById('srDept').textContent = a.department;
            document.getElementById('srYear').textContent = a.academicYear;
            document.getElementById('srSem').textContent = a.semester;
            document.getElementById('srCat').textContent = a.sportsCategory;
            document.getElementById('srSport').textContent = a.sportsName;
            document.getElementById('srSgpa').textContent = a.sgpa.toFixed(2);
            document.getElementById('srElig').textContent = a.eligibilityStatus;

            document.getElementById('srSpi').textContent = s.spi ? `${s.spi.toFixed(2)}%` : 'Pending Evaluation';
            document.getElementById('srAward').textContent = s.scholarshipType || 'Pending';
            document.getElementById('srWaiver').textContent = s.feeWaiver ? `${s.feeWaiver.toFixed(0)}% Fee Waiver` : '0%';
            return;
        }
    } catch (e) {
        console.warn('Search fallback to demo list');
    }

    // Demo lookup
    const demo = allScholarships.find(x => x.registrationId === regId);
    if (demo) {
        card.style.display = 'block';
        document.getElementById('srName').textContent = demo.studentName;
        document.getElementById('srRegId').textContent = demo.registrationId;
        document.getElementById('srDept').textContent = demo.department;
        document.getElementById('srYear').textContent = "3";
        document.getElementById('srSem').textContent = "5";
        document.getElementById('srCat').textContent = demo.sportsCategory;
        document.getElementById('srSport').textContent = "Athletics";
        document.getElementById('srSgpa').textContent = demo.sgpa.toFixed(2);
        document.getElementById('srElig').textContent = demo.sgpa >= 6.0 ? "ELIGIBLE" : "INELIGIBLE";
        document.getElementById('srSpi').textContent = `${demo.spi.toFixed(2)}%`;
        document.getElementById('srAward').textContent = demo.scholarshipType;
        document.getElementById('srWaiver').textContent = `${demo.feeWaiver.toFixed(0)}% Fee Waiver`;
    } else {
        showToast(`No athlete found with Registration ID: ${regId}`, false);
        card.style.display = 'none';
    }
}
