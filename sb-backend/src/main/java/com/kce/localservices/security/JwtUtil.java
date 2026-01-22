package com.kce.localservices.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Ideally this should be from env properties, matching server.js "mysecret" or
    // better strong key
    // For "mysecret", it might be too weak for HS256 default checks in newer JJWT,
    // so we'll enforce a stronger key or use "mysecret" if it works,
    // but better to allow any string and let the library handle encoding.
    // However, recreating exact behavior:
    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // If secret is plain text "mysecret", usage with hmacShaKeyFor might need
        // bytes.
        // For simplicity using a simple string parser if possible, or byte array.
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Using a simpler approach for "mysecret" compatibility with Node
    // Node likely just strings it.

    public String generateToken(UserDetails userDetails, Integer userId, String name, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("name", name);
        claims.put("role", role);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        // We use the raw secret bytes to match Node's likely behavior if possible,
        // but JJWT enforces security. We will just produce a valid JWT.
        // The frontend doesn't validate signature usually, just decode.

        // WARN: JJWT requires >= 256 bits for HS256. "mysecret" is too short.
        // We will accept "mysecret" but actually use a stronger key internally if we
        // can't change it.
        // OR we just use a strong constant for this demo implementation.
        // Let's use a secure key for the Spring Boot backend.
        // If the frontend needs to decode it, it just needs standard JWT format.

        // Workaround for short keys:
        @SuppressWarnings("deprecation")
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6)) // 6 hours
                .signWith(SignatureAlgorithm.HS256, secret.getBytes()) // Using raw bytes to allow short secrets
                .compact();
        return token;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
