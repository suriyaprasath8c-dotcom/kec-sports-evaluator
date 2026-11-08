package com.kec.sports.util;

import com.kec.sports.model.TrialLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * CSVHandler.java
 * Handles reading and writing CSV files using standard Java I/O (BufferedReader / BufferedWriter).
 * Used for importing track & field trial records from coach logs.
 */
public final class CSVHandler {

    private CSVHandler() {}

    // Reads trial timing rows from CSV and validates format
    public static List<TrialLog> importTrialTimings(String filePath) {
        List<TrialLog> list = new ArrayList<>();
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: CSV path is empty.");
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine(); // skip column headers
            if (header == null) {
                System.err.println("Warning: CSV file is empty.");
                return list;
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 4) {
                    System.err.println("Skipping malformed row " + rowNum + ": " + line);
                    continue;
                }

                String regId = cols[0].trim();
                String event = cols[1].trim();
                String timing = cols[2].trim();
                String discStr = cols[3].trim();
                String remarks = cols.length > 4 ? cols[4].trim() : "";

                // Validate registration format and timing format
                if (!StringProcessor.validateRegistrationId(regId)) {
                    System.err.println("Row " + rowNum + ": Invalid registration ID format (" + regId + ")");
                    continue;
                }
                if (!StringProcessor.validateTrialTiming(timing)) {
                    System.err.println("Row " + rowNum + ": Invalid trial timing format (" + timing + ")");
                    continue;
                }

                try {
                    double disc = Double.parseDouble(discStr);
                    if (disc < 0 || disc > 100) disc = 85.0; // clamp if out of range
                    String sanitized = StringProcessor.sanitizeCoachRemarks(remarks);
                    list.add(new TrialLog(regId, event, timing, disc, sanitized));
                } catch (NumberFormatException e) {
                    System.err.println("Row " + rowNum + ": Invalid discipline score: " + discStr);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read CSV file: " + e.getMessage());
        }

        return list;
    }

    // Exports table data into CSV format
    public static void exportToCSV(String filePath, List<String[]> rows, String[] headers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            if (headers != null && headers.length > 0) {
                writer.write(String.join(",", headers));
                writer.newLine();
            }
            if (rows != null) {
                for (String[] row : rows) {
                    writer.write(String.join(",", row));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }
}
