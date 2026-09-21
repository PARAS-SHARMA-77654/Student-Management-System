package com.soarjmi.seats.service;

import com.soarjmi.seats.model.AllocationResult;
import com.soarjmi.seats.model.Registration;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegistrationCsvService {

    public List<Registration> parse(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.isBlank()) {
            throw new IllegalArgumentException("The CSV file is empty.");
        }
        List<String> header = splitLine(stripBom(headerLine));
        int nameCol = columnIndex(header, "name");
        int emailCol = columnIndex(header, "email");
        int timeCol = columnIndex(header, "timestamp");
        int widest = Math.max(nameCol, Math.max(emailCol, timeCol));

        List<Registration> registrations = new ArrayList<>();
        int rowNumber = 1;
        String line;
        while ((line = reader.readLine()) != null) {
            rowNumber++;
            if (line.isBlank()) {
                continue;
            }
            List<String> cells = splitLine(line);
            if (cells.size() <= widest) {
                throw new IllegalArgumentException("Line " + rowNumber + " has too few columns.");
            }
            registrations.add(new Registration(
                    cells.get(nameCol),
                    cells.get(emailCol),
                    parseTimestamp(cells.get(timeCol), rowNumber)));
        }

        if (registrations.isEmpty()) {
            throw new IllegalArgumentException("The CSV file has a header but no registrations.");
        }
        return registrations;
    }

    public String export(AllocationResult result) {
        StringBuilder csv = new StringBuilder("name,email,timestamp,status\n");
        for (Registration r : result.getConfirmed()) {
            appendRow(csv, r, "Confirmed");
        }
        for (Registration r : result.getWaitlist()) {
            appendRow(csv, r, "Waitlist");
        }
        return csv.toString();
    }

    private void appendRow(StringBuilder csv, Registration r, String status) {
        csv.append(escape(r.getName())).append(',')
                .append(escape(r.getEmail())).append(',')
                .append(r.getFormattedTimestamp()).append(',')
                .append(status).append('\n');
    }

    private LocalDateTime parseTimestamp(String value, int rowNumber) {
        try {
            return LocalDateTime.parse(value, Registration.TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Line " + rowNumber + ": '" + value
                    + "' is not a valid timestamp (expected yyyy-MM-dd HH:mm:ss).");
        }
    }

    private int columnIndex(List<String> header, String column) {
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).equalsIgnoreCase(column)) {
                return i;
            }
        }
        throw new IllegalArgumentException("The CSV header must contain a '" + column + "' column.");
    }

    private String stripBom(String line) {
        return line.startsWith("\uFEFF") ? line.substring(1) : line;
    }

    // Splits one CSV line on commas, leaving commas inside double quotes alone.
    private List<String> splitLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString().trim());
        return cells;
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
