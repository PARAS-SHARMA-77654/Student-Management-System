# Seat Allocation Engine (Web)

A small Spring Boot website that allocates event seats first come, first served and
puts everyone else on a waitlist. It is the web version of a command-line Python tool
that used a min-heap on registration timestamps.

## What it does

1. Upload a CSV of registrations (or use the built-in sample of 10 students).
2. Enter the seat capacity.
3. See the summary, the full attendee manifest with a status for each person, and
   download the manifest as `output.csv`.

## Requirements

- JDK 17 or newer
- Maven 3.6 or newer

## Run it

```bash
mvn spring-boot:run
```

Then open http://localhost:8080 in a browser.

## Run the tests

```bash
mvn test
```

## Input format

```
name,email,timestamp
Sheema Shariq,sheema25@jmi.ac.in,2026-08-18 09:12:00
Priya Singh,priya@jmi.ac.in,2026-08-18 09:45:00
```

Timestamps must look like `yyyy-MM-dd HH:mm:ss`. Rows can be in any order, because
the heap sorts them by timestamp.

## Project layout

```
src/main/java/com/soarjmi/seats
    SeatAllocationApplication.java      entry point
    controller/AllocationController     pages, upload and download
    model/Registration                  one person's registration
    model/AllocationResult              confirmed list + waitlist
    service/AllocationService           the min-heap allocation logic
    service/RegistrationCsvService      CSV reading and writing
src/main/resources
    templates/index.html                upload form
    templates/result.html               summary and manifest table
    static/css/style.css                all styling
    sample/input.csv                    sample registrations
src/test/java/.../AllocationServiceTest.java
```

## Complexity

Every registration is pushed onto a heap and popped off again, and each of those
operations is O(log n), so the whole allocation is O(n log n) time and O(n) space.
