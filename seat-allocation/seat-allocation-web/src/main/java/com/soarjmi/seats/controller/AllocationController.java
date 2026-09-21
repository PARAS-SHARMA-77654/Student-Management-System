package com.soarjmi.seats.controller;

import com.soarjmi.seats.model.AllocationResult;
import com.soarjmi.seats.model.Registration;
import com.soarjmi.seats.service.AllocationService;
import com.soarjmi.seats.service.RegistrationCsvService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class AllocationController {

    private static final int DEFAULT_SEAT_CAP = 5;
    private static final String SAMPLE_FILE = "sample/input.csv";
    private static final String MANIFEST_ATTRIBUTE = "manifestCsv";

    private final AllocationService allocationService;
    private final RegistrationCsvService csvService;

    public AllocationController(AllocationService allocationService, RegistrationCsvService csvService) {
        this.allocationService = allocationService;
        this.csvService = csvService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("cap", DEFAULT_SEAT_CAP);
        return "index";
    }

    @PostMapping("/allocate")
    public String allocate(@RequestParam("cap") int cap,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           HttpSession session,
                           Model model) {
        model.addAttribute("cap", cap);

        if (cap < 1) {
            return showError(model, "Seat capacity must be at least 1.");
        }

        boolean useSample = file == null || file.isEmpty();
        try {
            List<Registration> registrations = useSample ? readSample() : readUpload(file);
            AllocationResult result = allocationService.allocate(registrations, cap);

            session.setAttribute(MANIFEST_ATTRIBUTE, csvService.export(result));
            model.addAttribute("result", result);
            model.addAttribute("source", useSample ? "input.csv (sample data)" : file.getOriginalFilename());
            return "result";
        } catch (IllegalArgumentException e) {
            return showError(model, e.getMessage());
        } catch (IOException e) {
            return showError(model, "Something went wrong while reading the file.");
        }
    }

    @GetMapping("/manifest.csv")
    public ResponseEntity<byte[]> download(HttpSession session) {
        String csv = (String) session.getAttribute(MANIFEST_ATTRIBUTE);
        if (csv == null) {
            // nothing has been allocated in this session yet
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String fileTooLarge(Model model) {
        model.addAttribute("cap", DEFAULT_SEAT_CAP);
        return showError(model, "That file is too large. Please keep it under 1 MB.");
    }

    private List<Registration> readSample() throws IOException {
        try (InputStream in = new ClassPathResource(SAMPLE_FILE).getInputStream()) {
            return csvService.parse(in);
        }
    }

    private List<Registration> readUpload(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            return csvService.parse(in);
        }
    }

    private String showError(Model model, String message) {
        model.addAttribute("error", message);
        return "index";
    }
}
