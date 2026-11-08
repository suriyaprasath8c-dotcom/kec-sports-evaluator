package com.kec.sports.util;

import com.kec.sports.model.TrialLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing and exporting CSV data.
 * Uses raw Java I/O (BufferedReader / BufferedWriter) — no third-party CSV libraries.
 */
public final class CSVHandler {

    /** Expected number of columns in the trial-timings CSV. */
    private static final int EXPECTED_TRIAL_COLUMNS = 5;

    /** CSV column delimiter. */
    private static final String DELIMITER = ",";

    // Private constructor — utility class should not be instantiated
    private CSVHandler() {
        throw new UnsupportedOperationException("Utility class — do not instantiate.");
    }

    // ── Import ─────────────────────────────────────────────────────────────

    /**
     * Imports trial timing data from a CSV file.
     * <p>
     * Expected CSV columns (header row required):
     * <pre>registration_id, event_name, trial_timing, discipline_score, coach_remarks</pre>
     * <p>
     * Each row is validated:
     * <ul>
     *   <li>Registration ID must match the KEC format</li>
     *   <li>Event name must not be blank</li>
     *   <li>Trial timing must be in MM:SS.ms format</li>
     *   <li>Discipline score must be a valid number between 0 and 100</li>
     * </ul>
     * Invalid rows are skipped with an error message logged to {@code System.err}.
     *
     * @param filePath the absolute or relative path to the CSV file
     * @return a list of validated {@link TrialLog} objects
     */
    public static List<TrialLog> importTrialTimings(String filePath) {
        List<TrialLog> trialLogs = new ArrayList<>();

        if (filePath == null || filePath.trim().isEmpty()) {
            log("ERROR: File path cannot be null or empty.");
            return trialLogs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log("ERROR: CSV file is empty — " + filePath);
                return trialLogs;
            }

            // Validate header (informational; we still try to parse rows)
            validateHeader(headerLine);

            String line;
            int lineNumber = 1; // header was line 1
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip blank lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    TrialLog trialLog = parseTrialRow(line, lineNumber);
                    if (trialLog != null) {
                        trialLogs.add(trialLog);
                    }
                } catch (Exception e) {
                    log("ERROR: Skipping line " + lineNumber + " — " + e.getMessage());
                }
            }

            log("Import complete: " + trialLogs.size() + " valid trial record(s) loaded from "
                    + filePath);

        } catch (IOException e) {
            log("ERROR: Failed to read CSV file '" + filePath + "': " + e.getMessage());
        }

        return trialLogs;
    }

    // ── Export ──────────────────────────────────────────────────────────────

    /**
     * Exports data rows to a CSV file, prepended with a header row.
     *
     * @param filePath the path to write the CSV file to
     * @param data     a list of string arrays, where each array is one row
     * @param headers  the column header names
     */
    public static void exportToCSV(String filePath, List<String[]> data, String[] headers) {
        if (filePath == null || filePath.trim().isEmpty()) {
            log("ERROR: Export file path cannot be null or empty.");
            return;
        }
        if (headers == null || headers.length == 0) {
            log("ERROR: Headers array cannot be null or empty.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header row
            writer.write(joinCSV(headers));
            writer.newLine();

            // Write data rows
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    String[] row = data.get(i);
                    if (row != null) {
                        writer.write(joinCSV(row));
                        writer.newLine();
                    } else {
                        log("WARNING: Null row at index " + i + " — skipped.");
                    }
                }
            }

            log("Export complete: " + (data == null ? 0 : data.size())
                    + " row(s) written to " + filePath);

        } catch (IOException e) {
            log("ERROR: Failed to write CSV file '" + filePath + "': " + e.getMessage());
        }
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Validates the CSV header line against the expected column names.
     * Logs a warning if the header does not match; parsing still proceeds.
     */
    private static void validateHeader(String headerLine) {
        String expected = "registration_id,event_name,trial_timing,discipline_score,coach_remarks";
        String normalised = headerLine.trim().toLowerCase().replaceAll("\\s+", "");
        String expectedNorm = expected.replaceAll("\\s+", "");
        if (!normalised.equals(expectedNorm)) {
            log("WARNING: CSV header does not match expected format.");
            log("  Expected: " + expected);
            log("  Found:    " + headerLine.trim());
        }
    }

    /**
     * Parses a single CSV data row into a {@link TrialLog} object.
     * Returns {@code null} and logs an error when validation fails.
     *
     * @param line       the raw CSV line
     * @param lineNumber the 1-based line number (for error messages)
     * @return a validated TrialLog, or null if the row is invalid
     */
    private static TrialLog parseTrialRow(String line, int lineNumber) {
        String[] columns = line.split(DELIMITER, -1); // -1 keeps trailing empty strings

        if (columns.length < EXPECTED_TRIAL_COLUMNS) {
            log("ERROR: Line " + lineNumber + " has " + columns.length
                    + " column(s); expected " + EXPECTED_TRIAL_COLUMNS + ". Skipping.");
            return null;
        }

        String registrationId = columns[0].trim();
        String eventName = columns[1].trim();
        String trialTiming = columns[2].trim();
        String disciplineScoreStr = columns[3].trim();
        // Coach remarks may contain commas in free text; rejoin remaining columns
        String coachRemarks = rejoinRemainingColumns(columns, 4);

        // ── Validate registration ID ──
        if (!StringProcessor.validateRegistrationId(registrationId)) {
            log("ERROR: Line " + lineNumber + " — invalid registration ID: '"
                    + registrationId + "'. Skipping.");
            return null;
        }

        // ── Validate event name ──
        if (eventName.isEmpty()) {
            log("ERROR: Line " + lineNumber + " — event name is blank. Skipping.");
            return null;
        }

        // ── Validate trial timing ──
        if (!StringProcessor.validateTrialTiming(trialTiming)) {
            log("ERROR: Line " + lineNumber + " — invalid trial timing: '"
                    + trialTiming + "'. Skipping.");
            return null;
        }

        // ── Validate discipline score ──
        double disciplineScore;
        try {
            disciplineScore = Double.parseDouble(disciplineScoreStr);
        } catch (NumberFormatException e) {
            log("ERROR: Line " + lineNumber + " — discipline score is not a number: '"
                    + disciplineScoreStr + "'. Skipping.");
            return null;
        }
        if (disciplineScore < 0 || disciplineScore > 100) {
            log("ERROR: Line " + lineNumber + " — discipline score out of range (0-100): "
                    + disciplineScore + ". Skipping.");
            return null;
        }

        // ── Sanitise coach remarks ──
        String sanitizedRemarks = StringProcessor.sanitizeCoachRemarks(coachRemarks);

        return new TrialLog(registrationId, eventName, trialTiming,
                disciplineScore, sanitizedRemarks);
    }

    /**
     * Rejoins columns from index {@code startIndex} onward with the delimiter,
     * handling cases where free-text fields contain commas.
     */
    private static String rejoinRemainingColumns(String[] columns, int startIndex) {
        if (startIndex >= columns.length) {
            return "";
        }
        StringBuilder sb = new StringBuilder(columns[startIndex].trim());
        for (int i = startIndex + 1; i < columns.length; i++) {
            sb.append(DELIMITER).append(columns[i]);
        }
        return sb.toString().trim();
    }

    /**
     * Joins a string array into a single CSV line.
     * Values containing commas, quotes, or newlines are enclosed in double-quotes
     * with internal quotes escaped.
     */
    private static String joinCSV(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(DELIMITER);
            }
            String value = (values[i] != null) ? values[i] : "";
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                // Escape double-quotes and wrap in quotes
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * Logs a message to System.err with a [CSVHandler] prefix.
     */
    private static void log(String message) {
        System.err.println("[CSVHandler] " + message);
    }
}
