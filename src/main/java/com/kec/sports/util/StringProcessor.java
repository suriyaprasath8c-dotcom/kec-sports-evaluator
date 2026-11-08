package com.kec.sports.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * StringProcessor.java
 * Utility helpers for KEC athlete ID generation, regex format checks,
 * trial timing conversion (MM:SS.ms), and coach remark cleaning.
 */
public final class StringProcessor {

    // KEC Registration ID pattern: KEC-SPT-YYYY-NNN (e.g. KEC-SPT-2026-001)
    private static final Pattern REGISTRATION_ID_PATTERN =
            Pattern.compile("^KEC-SPT-\\d{4}-\\d{3}$");

    // Timing pattern for track events: MM:SS.ms (e.g. 00:11.25 or 02:15.340)
    private static final Pattern TRIAL_TIMING_PATTERN =
            Pattern.compile("^(\\d{2}):(\\d{2})\\.(\\d{2,3})$");

    // Standard benchmark times in seconds for college track events
    private static final Map<String, Double> EVENT_BENCHMARKS = new HashMap<>();

    static {
        EVENT_BENCHMARKS.put("100m Sprint", 11.00);
        EVENT_BENCHMARKS.put("200m Sprint", 22.50);
        EVENT_BENCHMARKS.put("400m Sprint", 50.00);
        EVENT_BENCHMARKS.put("400m Relay", 45.00);
        EVENT_BENCHMARKS.put("800m Run", 120.00);
        EVENT_BENCHMARKS.put("1500m Run", 240.00);
        EVENT_BENCHMARKS.put("110m Hurdles", 14.50);
        EVENT_BENCHMARKS.put("Long Jump", 10.00);
        EVENT_BENCHMARKS.put("High Jump", 5.00);
        EVENT_BENCHMARKS.put("Shot Put", 15.00);
    }

    private StringProcessor() {
        // utility class
    }

    // Generates formatted ID: KEC-SPT-2026-001
    public static String generateRegistrationId(int sequenceNumber, int year) {
        if (sequenceNumber < 0 || sequenceNumber > 999) {
            throw new IllegalArgumentException("Sequence number out of range: " + sequenceNumber);
        }
        return String.format("KEC-SPT-%04d-%03d", year, sequenceNumber);
    }

    // Validates ID against KEC format regex
    public static boolean validateRegistrationId(String id) {
        if (id == null) return false;
        return REGISTRATION_ID_PATTERN.matcher(id.trim()).matches();
    }

    // Converts "MM:SS.ms" (e.g. "02:15.45") into total seconds as double
    public static double parseTrialTiming(String timing) {
        if (timing == null || timing.trim().isEmpty()) {
            throw new IllegalArgumentException("Timing string cannot be empty.");
        }

        Matcher matcher = TRIAL_TIMING_PATTERN.matcher(timing.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid timing format: '" + timing + "'. Expected MM:SS.ms");
        }

        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));
        String msStr = matcher.group(3);

        double fraction = (msStr.length() == 2)
                ? Integer.parseInt(msStr) / 100.0
                : Integer.parseInt(msStr) / 1000.0;

        return (minutes * 60.0) + seconds + fraction;
    }

    public static boolean validateTrialTiming(String timing) {
        if (timing == null) return false;
        return TRIAL_TIMING_PATTERN.matcher(timing.trim()).matches();
    }

    // Lookup benchmark for specific athletic event
    public static double getBenchmarkTime(String eventName) {
        if (eventName == null) return 60.0;
        for (Map.Entry<String, Double> entry : EVENT_BENCHMARKS.entrySet()) {
            if (eventName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return 60.0; // fallback default
    }

    // Lower actual time gives a higher score up to 100 max
    public static double normalizeTrialScore(double actualTimeSeconds, double benchmarkTimeSeconds) {
        if (actualTimeSeconds <= 0 || benchmarkTimeSeconds <= 0) {
            return 0.0;
        }
        double score = (benchmarkTimeSeconds / actualTimeSeconds) * 100.0;
        if (score > 100.0) score = 100.0;
        if (score < 0.0) score = 0.0;
        return Math.round(score * 100.0) / 100.0;
    }

    // Strips HTML tags and disallows unwanted characters from coach feedback
    public static String sanitizeCoachRemarks(String remarks) {
        if (remarks == null) return "";
        // Remove HTML tags, trim, and keep standard alphanumeric and punctuation
        String cleaned = remarks.replaceAll("<[^>]*>", "").trim();
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9\\s.,;:!?'\"()-]", "");
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500);
        }
        return cleaned;
    }

    public static String formatScore(double score) {
        return String.format("%.2f", score);
    }
}
