package com.example.loganalyzer.parser;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexLogParser implements LogParser {

    private static final Pattern STANDARD_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2}(?:[.,]\\d{3})?(?:Z|[+-]\\d{2}:?\\d{2})?)\\s+(?<level>TRACE|DEBUG|INFO|WARN|ERROR|FATAL)\\s+(?<service>[A-Za-z0-9_.-]+)\\s+-\\s+(?<message>.*)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BRACKET_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?(?:Z|[+-]\\d{2}:?\\d{2})?)\\s+\\[(?<level>TRACE|DEBUG|INFO|WARN|ERROR|FATAL)]\\s+\\[(?<service>[^]]+)]\\s+(?<message>.*)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DEVICE_STYLE_PATTERN = Pattern.compile(
            "^\\[(?<timestamp>\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2})]\\s+(?<service>[A-Za-z0-9_.-]+)(?:\\s+\\*\\d+)?\\s+-\\s+(?<message>.*)$",
            Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter SHORT_DEVICE_DATE = DateTimeFormatter.ofPattern("MM.dd");
    private static final DateTimeFormatter SHORT_DEVICE_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile("([A-Z][A-Za-z0-9]+(?:Exception|Error))");
    private static final Pattern EXECUTABLE_PATTERN = Pattern.compile("\\b([A-Za-z0-9_.-]+\\.(?:exe|dll|jar|bin))\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERVICE_LIKE_PATTERN = Pattern.compile("\\b([A-Z][A-Za-z]+Service|[A-Z][A-Za-z]+Controller|[A-Z][A-Za-z]+Repository)\\b");
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("\\b([A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+){1,})\\b");

    @Override
    public List<ParsedLogLine> parse(String rawLogs) {
        List<ParsedLogLine> lines = new ArrayList<>();
        String[] rawLines = rawLogs.split("\\r?\\n");
        for (String line : rawLines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            lines.add(parseLine(line));
        }
        return lines;
    }

    private ParsedLogLine parseLine(String rawLine) {
        Matcher standard = STANDARD_PATTERN.matcher(rawLine);
        if (standard.matches()) {
            return buildMatch(rawLine, standard);
        }
        Matcher bracket = BRACKET_PATTERN.matcher(rawLine);
        if (bracket.matches()) {
            return buildMatch(rawLine, bracket);
        }
        Matcher deviceStyle = DEVICE_STYLE_PATTERN.matcher(rawLine);
        if (deviceStyle.matches()) {
            return ParsedLogLine.builder()
                    .timestamp(parseTimestamp(deviceStyle.group("timestamp")))
                    .rawLine(rawLine)
                    .message(deviceStyle.group("message"))
                    .normalizedMessage(normalizeMessage(deviceStyle.group("message")))
                    .exceptionType(extractExceptionType(deviceStyle.group("message")))
                    .level(inferLevel(deviceStyle.group("message")))
                    .serviceName(deviceStyle.group("service"))
                    .build();
        }
        return ParsedLogLine.builder()
                .rawLine(rawLine)
                .message(extractMessage(rawLine))
                .normalizedMessage(normalizeMessage(extractMessage(rawLine)))
                .exceptionType(extractExceptionType(rawLine))
                .level(inferLevel(rawLine))
                .serviceName(inferService(rawLine))
                .build();
    }

    private ParsedLogLine buildMatch(String rawLine, Matcher matcher) {
        String message = matcher.group("message");
        return ParsedLogLine.builder()
                .timestamp(parseTimestamp(matcher.group("timestamp")))
                .level(matcher.group("level").toUpperCase(Locale.ROOT))
                .serviceName(matcher.group("service"))
                .message(message)
                .exceptionType(extractExceptionType(message))
                .rawLine(rawLine)
                .normalizedMessage(normalizeMessage(message))
                .build();
    }

    private OffsetDateTime parseTimestamp(String value) {
        if (value == null) {
            return null;
        }
        if (value.matches("\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            try {
                MonthDay monthDay = MonthDay.parse(value.substring(0, 5), SHORT_DEVICE_DATE);
                LocalTime localTime = LocalTime.parse(value.substring(6), SHORT_DEVICE_TIME);
                LocalDateTime localDateTime = LocalDateTime.of(
                        Year.now(ZoneOffset.UTC).getValue(),
                        monthDay.getMonthValue(),
                        monthDay.getDayOfMonth(),
                        localTime.getHour(),
                        localTime.getMinute(),
                        localTime.getSecond()
                );
                return localDateTime.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                DateTimeFormatter.ISO_ZONED_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME || formatter == DateTimeFormatter.ISO_ZONED_DATE_TIME) {
                    return OffsetDateTime.parse(value, formatter);
                }
                LocalDateTime localDateTime = LocalDateTime.parse(value, formatter);
                return localDateTime.atOffset(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String extractExceptionType(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = EXCEPTION_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String inferLevel(String rawLine) {
        String upper = rawLine.toUpperCase(Locale.ROOT);
        if (upper.contains("ERROR") || upper.contains("EXCEPTION") || upper.contains("FAILED")) {
            return "ERROR";
        }
        if (upper.contains("WARN")) {
            return "WARN";
        }
        if (upper.contains("DEBUG")) {
            return "DEBUG";
        }
        return "INFO";
    }

    private String inferService(String rawLine) {
        Matcher executableMatcher = EXECUTABLE_PATTERN.matcher(rawLine);
        if (executableMatcher.find()) {
            return executableMatcher.group(1);
        }

        Matcher serviceMatcher = SERVICE_LIKE_PATTERN.matcher(rawLine);
        if (serviceMatcher.find()) {
            return serviceMatcher.group(1);
        }

        Matcher deviceStyle = DEVICE_STYLE_PATTERN.matcher(rawLine);
        if (deviceStyle.matches()) {
            return deviceStyle.group("service");
        }

        Matcher hostnameMatcher = HOSTNAME_PATTERN.matcher(rawLine);
        if (hostnameMatcher.find()) {
            return hostnameMatcher.group(1);
        }

        return "Unknown";
    }

    private String extractMessage(String rawLine) {
        if (rawLine == null) {
            return null;
        }
        Matcher deviceStyle = DEVICE_STYLE_PATTERN.matcher(rawLine);
        if (deviceStyle.matches()) {
            return deviceStyle.group("message");
        }
        int separatorIndex = rawLine.indexOf(" - ");
        if (separatorIndex >= 0 && separatorIndex + 3 < rawLine.length()) {
            return rawLine.substring(separatorIndex + 3).trim();
        }
        return rawLine.trim();
    }

    private String normalizeMessage(String message) {
        return message == null ? null : message
                .replaceAll("(?i)request[-_ ]?id\\s*[:=]?\\s*[A-Za-z0-9-]+", "request-id=<id>")
                .replaceAll("\\b[0-9a-fA-F]{8}-[0-9a-fA-F-]{27,}\\b", "<id>")
                .replaceAll("\\b[0-9a-fA-F]{8,}\\b", "<id>")
                .replaceAll("\\s*\\*\\d+\\s*", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
