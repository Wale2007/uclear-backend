package ng.edu.futa.uclear.dto;

/**
 * Type-safe Response DTO for authentication.
 * Eliminates raw Map<String, Object> and runtime string typos.
 */
public record AuthResponse(
    String token,
    String role,
    String id,
    String name,
    String email,
    String phone,
    String department,
    String faculty,
    String level,
    String matricNo,
    String staffId,
    String title
) {}
