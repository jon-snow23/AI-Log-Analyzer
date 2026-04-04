package com.example.loganalyzer.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexLogParserTest {

    private final RegexLogParser parser = new RegexLogParser();

    @Test
    void parsesStandardLine() {
        List<ParsedLogLine> result = parser.parse("2026-04-04 10:01:15 ERROR PaymentService - Timeout while connecting to payment provider");

        assertEquals(1, result.size());
        ParsedLogLine line = result.get(0);
        assertEquals("ERROR", line.getLevel());
        assertEquals("PaymentService", line.getServiceName());
        assertEquals("timeout while connecting to payment provider", line.getNormalizedMessage());
        assertNotNull(line.getTimestamp());
    }

    @Test
    void fallsBackForUnmatchedLine() {
        List<ParsedLogLine> result = parser.parse("weird unformatted NullPointerException happened for request 12345");

        assertEquals(1, result.size());
        ParsedLogLine line = result.get(0);
        assertEquals("ERROR", line.getLevel());
        assertEquals("NullPointerException", line.getExceptionType());
        assertEquals("Unknown", line.getServiceName());
    }

    @Test
    void parsesProxyStyleDeviceLogAndKeepsReadableMessage() {
        String rawLine = "[06.04 18:23:11] chrome.exe *123 - mtalk.google.com:5228 error : could not connect through proxy proxy.cse.cuhk.edu.hk:8080 - proxy server cannot establish a connection with the target, status code 403";

        List<ParsedLogLine> result = parser.parse(rawLine);

        assertEquals(1, result.size());
        ParsedLogLine line = result.get(0);
        assertEquals("ERROR", line.getLevel());
        assertEquals("chrome.exe", line.getServiceName());
        assertEquals("mtalk.google.com:5228 error : could not connect through proxy proxy.cse.cuhk.edu.hk:8080 - proxy server cannot establish a connection with the target, status code 403", line.getMessage());
        assertFalse(line.getNormalizedMessage().contains("<num>"));
        assertTrue(line.getNormalizedMessage().contains("status code 403"));
    }

    @Test
    void parsesShortProxifierTimestampIntoConcreteDateTime() {
        String rawLine = "[10.30 16:49:06] chrome.exe - proxy.cse.cuhk.edu.hk:5070 open through proxy proxy.cse.cuhk.edu.hk:5070 HTTPS";

        ParsedLogLine line = parser.parse(rawLine).get(0);

        assertNotNull(line.getTimestamp());
        assertEquals(10, line.getTimestamp().getMonthValue());
        assertEquals(30, line.getTimestamp().getDayOfMonth());
        assertEquals(16, line.getTimestamp().getHour());
        assertEquals(49, line.getTimestamp().getMinute());
        assertEquals(6, line.getTimestamp().getSecond());
    }
}
