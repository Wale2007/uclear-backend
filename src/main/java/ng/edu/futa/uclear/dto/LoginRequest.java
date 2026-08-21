package ng.edu.futa.uclear.dto;

/**
 * Type-safe Request DTO for user login.
 * Replaces raw Map<String, String> to prevent spelling errors and provide compile-time safety.
 */
public record LoginRequest(
    String credential,
    String password,
    String role
) {}
