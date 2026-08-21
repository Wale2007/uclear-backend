package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.model.Profile;
import ng.edu.futa.uclear.repository.ProfileRepository;
import ng.edu.futa.uclear.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/auth/login
     * Body: { "credential": "SEN/22/9292", "password": "password123", "role": "student" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String credential = body.get("credential") != null ? body.get("credential").trim() : "";
        String password   = body.get("password") != null ? body.get("password").trim() : "";
        String role       = body.get("role") != null ? body.get("role").trim() : "";

        if (credential.isEmpty() || password.isEmpty() || role.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Credential, password, and role are required."));
        }

        // Look up profile by matric_no (student), staff_id (staff), or email/staff_id (admin)
        Optional<Profile> profileOpt;
        if (role.equalsIgnoreCase("admin")) {
            profileOpt = profileRepository.findByEmail(credential)
                    .or(() -> profileRepository.findByStaffId(credential));
        } else if (role.equalsIgnoreCase("student")) {
            profileOpt = profileRepository.findByMatricNo(credential);
        } else {
            profileOpt = profileRepository.findByStaffId(credential);
        }

        if (profileOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials. Please verify your details and try again."));
        }

        Profile profile = profileOpt.get();

        // Strict role validation — prevent students or staff from accessing admin role and vice-versa
        if (!profile.getRole().name().equalsIgnoreCase(role)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials. Please verify your details and try again."));
        }

        // Verify password (BCrypt)
        if (!passwordEncoder.matches(password, profile.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials. Please verify your details and try again."));
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(profile);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("role", profile.getRole().name());
        response.put("id", profile.getId());
        response.put("name", profile.getName());
        response.put("email", profile.getEmail());
        response.put("phone", profile.getPhone() != null ? profile.getPhone() : "");
        response.put("department", profile.getDepartment() != null ? profile.getDepartment() : "");
        response.put("faculty", profile.getFaculty() != null ? profile.getFaculty() : "");
        response.put("level", profile.getLevel() != null ? profile.getLevel() : "");
        response.put("matricNo", profile.getMatricNo() != null ? profile.getMatricNo() : "");
        response.put("staffId", profile.getStaffId() != null ? profile.getStaffId() : "");
        response.put("title", profile.getTitle() != null ? profile.getTitle() : "");

        return ResponseEntity.ok(response);
    }
}
