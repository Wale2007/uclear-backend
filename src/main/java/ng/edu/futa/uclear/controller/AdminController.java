package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.config.DatabaseSeeder;
import ng.edu.futa.uclear.model.Due;
import ng.edu.futa.uclear.model.Profile;
import ng.edu.futa.uclear.model.Receipt;
import ng.edu.futa.uclear.repository.DueRepository;
import ng.edu.futa.uclear.repository.ProfileRepository;
import ng.edu.futa.uclear.repository.ReceiptRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProfileRepository profileRepository;
    private final DueRepository dueRepository;
    private final ReceiptRepository receiptRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatabaseSeeder databaseSeeder;

    /**
     * GET /api/admin/stats
     * Dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        databaseSeeder.seedAllIfMissing();
        long totalStudents = profileRepository.findAll().stream()
                .filter(p -> p.getRole() == Profile.Role.student).count();
        long totalStaff = profileRepository.findAll().stream()
                .filter(p -> p.getRole() == Profile.Role.staff).count();
        long totalReceipts = receiptRepository.count();
        double totalRevenue = receiptRepository.findAll().stream()
                .mapToDouble(r -> r.getAmount().doubleValue()).sum();

        return ResponseEntity.ok(Map.of(
                "totalStudents", totalStudents,
                "totalStaff", totalStaff,
                "totalReceipts", totalReceipts,
                "totalRevenue", totalRevenue
        ));
    }

    /**
     * GET /api/admin/profiles
     * List all profiles
     */
    @GetMapping("/profiles")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        databaseSeeder.seedAllIfMissing();
        return ResponseEntity.ok(profileRepository.findAll());
    }

    /**
     * GET /api/admin/receipts
     * Returns all payment receipts for the Clearance Audit Ledger.
     */
    @GetMapping("/receipts")
    public ResponseEntity<List<Receipt>> getAllReceipts() {
        return ResponseEntity.ok(receiptRepository.findAll());
    }

    /**
     * POST /api/admin/profiles/bulk-csv
     * Upload a CSV file to bulk-import students or staff.
     *
     * CSV Format for students:
     * name,email,phone,matric_no,department,faculty,level,password
     *
     * CSV Format for staff:
     * name,title,email,phone,staff_id,department,faculty,password
     */
    @PostMapping("/profiles/bulk-csv")
    public ResponseEntity<?> bulkImportCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role) {

        List<Profile> imported = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String[] headers = reader.readNext(); // skip header row
            String[] line;
            int row = 1;

            while ((line = reader.readNext()) != null) {
                row++;
                if (line.length == 0 || (line.length == 1 && line[0].isBlank())) continue;

                try {
                    Profile profile = new Profile();
                    profile.setId(UUID.randomUUID().toString());
                    profile.setRole(Profile.Role.valueOf(role.toLowerCase()));

                    if (role.equalsIgnoreCase("student")) {
                        // Expected: name,email,phone,matric_no,department,faculty,level,password
                        profile.setName(getCol(line, 0, "Student " + row));
                        profile.setEmail(getCol(line, 1, "student" + row + "@futa.edu.ng"));
                        profile.setPhone(getCol(line, 2, ""));
                        profile.setMatricNo(getCol(line, 3, "MAT/" + row));
                        profile.setDepartment(getCol(line, 4, "General"));
                        profile.setFaculty(getCol(line, 5, "General"));
                        profile.setLevel(getCol(line, 6, "100 Level"));
                        String rawPass = getCol(line, 7, "password123");
                        profile.setPassword(passwordEncoder.encode(rawPass.isEmpty() ? "password123" : rawPass));
                    } else {
                        // Expected: name,title,email,phone,staff_id,department,faculty,password
                        profile.setName(getCol(line, 0, "Staff " + row));
                        profile.setTitle(getCol(line, 1, "Lecturer"));
                        profile.setEmail(getCol(line, 2, "staff" + row + "@futa.edu.ng"));
                        profile.setPhone(getCol(line, 3, ""));
                        profile.setStaffId(getCol(line, 4, "STF/" + row));
                        profile.setDepartment(getCol(line, 5, "General"));
                        profile.setFaculty(getCol(line, 6, "General"));
                        String rawPass = getCol(line, 7, "password123");
                        profile.setPassword(passwordEncoder.encode(rawPass.isEmpty() ? "password123" : rawPass));
                    }

                    imported.add(profileRepository.save(profile));
                } catch (Exception e) {
                    errors.add("Row " + row + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse CSV: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "imported", imported.size(),
                "errors", errors
        ));
    }

    private String getCol(String[] line, int idx, String defaultVal) {
        if (line != null && idx >= 0 && idx < line.length && line[idx] != null && !line[idx].isBlank()) {
            return line[idx].trim();
        }
        return defaultVal;
    }
}
