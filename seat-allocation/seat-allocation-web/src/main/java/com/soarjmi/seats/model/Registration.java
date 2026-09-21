package com.soarjmi.seats.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Registration {

    public static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String name;
    private final String email;
    private final LocalDateTime timestamp;

    public Registration(String name, String email, LocalDateTime timestamp) {
        this.name = name;
        this.email = email;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(TIMESTAMP_FORMAT);
    }
}
