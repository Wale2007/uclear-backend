package ng.edu.futa.uclear.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ng.edu.futa.uclear.model.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(keyBytes);
            } catch (Exception e) {
                byte[] padded = new byte[32];
                System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
                keyBytes = padded;
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Profile profile) {
        String subject = profile.getId();
        return Jwts.builder()
                .subject(subject)
                .claim("role", profile.getRole().name())
                .claim("name", profile.getName())
                .claim("email", profile.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractProfileId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
