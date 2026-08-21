package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.dto.AuthResponse;
import ng.edu.futa.uclear.dto.LoginRequest;
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
     * Uses type-safe LoginRequest and returns type-safe AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String credential = request.credential() != null ? request.credential().trim() : "";
        String password   = request.password() != null ? request.password().trim() : "";
        String role       = request.role() != null ? request.role().trim() : "";

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

        // Type-safe DTO response (no strings, compile-time verified)
        AuthResponse response = new AuthResponse(
            token,
            profile.getRole().name(),
            profile.getId(),
            profile.getName(),
            profile.getEmail(),
            profile.getPhone() != null ? profile.getPhone() : "",
            profile.getDepartment() != null ? profile.getDepartment() : "",
            profile.getFaculty() != null ? profile.getFaculty() : "",
            profile.getLevel() != null ? profile.getLevel() : "",
            profile.getMatricNo() != null ? profile.getMatricNo() : "",
            profile.getStaffId() != null ? profile.getStaffId() : "",
            profile.getTitle() != null ? profile.getTitle() : ""
        );

        return ResponseEntity.ok(response);
    }
}
