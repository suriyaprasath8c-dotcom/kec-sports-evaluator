package com.kec.sports.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing string processing, regex validation, timing conversion,
 * and text sanitization for KEC Sports Evaluator.
 */
public final class StringProcessor {

    /** Regex pattern for a valid KEC registration ID (e.g. KEC-SPT-2026-001) */
    private static final Pattern REGISTRATION_ID_PATTERN =
            Pattern.compile("^KEC-SPT-\\d{4}-\\d{3}$");

    /** Regex pattern for trial timing in MM:SS.ms format (2 or 3 digits for ms) */
    private static final Pattern TRIAL_TIMING_PATTERN =
            Pattern.compile("^(\\d{2}):(\\d{2})\\.(\\d{2,3})$");

    /** Pattern used to strip HTML tags from text */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    /** Pattern that matches characters NOT in the allowed set for coach remarks */
    private static final Pattern SPECIAL_CHAR_PATTERN =
            Pattern.compile("[^a-zA-Z0-9\\s.,;:!?'\"()-]");

    /** Maximum allowed length for sanitized coach remarks */
    private static final int MAX_REMARKS_LENGTH = 500;

    /** Standard benchmark times in seconds for Track and Field events */
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
        throw new UnsupportedOperationException("Utility class — do not instantiate.");
    }

    /**
     * Generates a KEC registration ID in the format KEC-SPT-{year}-{3-digit-seq}.
     */
    public static String generateRegistrationId(int sequenceNumber, int year) {
        if (sequenceNumber < 0 || sequenceNumber > 999) {
            throw new IllegalArgumentException(
                    "Sequence number must be between 0 and 999, got: " + sequenceNumber);
        }
        if (year < 1000 || year > 9999) {
            throw new IllegalArgumentException(
                    "Year must be a four-digit number, got: " + year);
        }
        return String.format("KEC-SPT-%04d-%03d", year, sequenceNumber);
    }

    /**
     * Validates whether the given string matches KEC-SPT-YYYY-NNN format.
     */
    public static boolean validateRegistrationId(String id) {
        if (id == null) {
            return false;
        }
        return REGISTRATION_ID_PATTERN.matcher(id.trim()).matches();
    }

    /**
     * Parses a trial timing string in MM:SS.ms format (e.g. "02:15.45" or "00:11.340")
     * and returns the total time in seconds as a double.
     */
    public static double parseTrialTiming(String timing) {
        if (timing == null || timing.trim().isEmpty()) {
            throw new IllegalArgumentException("Trial timing cannot be null or empty.");
        }

        Matcher matcher = TRIAL_TIMING_PATTERN.matcher(timing.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid trial timing format: '" + timing
                            + "'. Expected MM:SS.ms (e.g. 02:15.45 or 00:11.340).");
        }

        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));
        String msStr = matcher.group(3);
        double fraction;
        if (msStr.length() == 2) {
            fraction = Integer.parseInt(msStr) / 100.0;
        } else {
            fraction = Integer.parseInt(msStr) / 1000.0;
        }

        if (seconds >= 60) {
            throw new IllegalArgumentException(
                    "Seconds component must be 0-59, got: " + seconds);
        }

        return (minutes * 60.0) + seconds + fraction;
    }

    /**
     * Validates whether the timing string matches MM:SS.ms format.
     */
    public static boolean validateTrialTiming(String timing) {
        if (timing == null) {
            return false;
        }
        return TRIAL_TIMING_PATTERN.matcher(timing.trim()).matches();
    }

    /**
     * Gets configurable benchmark time in seconds for a specific event.
     */
    public static double getBenchmarkTime(String eventName) {
        if (eventName == null) return 60.0;
        for (Map.Entry<String, Double> entry : EVENT_BENCHMARKS.entrySet()) {
            if (eventName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return 60.0; // fallback standard 60-second benchmark
    }

    /**
     * Normalizes a trial timing into a 0-100 score relative to benchmark time.
     * Formula: score = (benchmarkTime / actualTime) * 100
     * Capped at 100.0 max and 0.0 min.
     * Lower actual time results in a higher score.
     */
    public static double normalizeTrialScore(double actualTimeSeconds, double benchmarkTimeSeconds) {
        if (actualTimeSeconds <= 0) {
            throw new IllegalArgumentException("Actual time must be positive: " + actualTimeSeconds);
        }
        if (benchmarkTimeSeconds <= 0) {
            throw new IllegalArgumentException("Benchmark time must be positive: " + benchmarkTimeSeconds);
        }
        double score = (benchmarkTimeSeconds / actualTimeSeconds) * 100.0;
        if (score > 100.0) score = 100.0;
        if (score < 0.0) score = 0.0;
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Sanitizes coach remarks by removing HTML tags, trimming, removing disallowed
     * characters, and truncating to max 500 characters.
     */
    public static String sanitizeCoachRemarks(String remarks) {
        if (remarks == null) {
            return "";
        }
        String sanitized = HTML_TAG_PATTERN.matcher(remarks).replaceAll("");
        sanitized = sanitized.trim();
        sanitized = SPECIAL_CHAR_PATTERN.matcher(sanitized).replaceAll("");
        if (sanitized.length() > MAX_REMARKS_LENGTH) {
            sanitized = sanitized.substring(0, MAX_REMARKS_LENGTH);
        }
        return sanitized;
    }

    /**
     * Formats a numeric score to two decimal places.
     */
    public static String formatScore(double score) {
        return String.format("%.2f", score);
    }
}
