package org.backend.utill;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.backend.model.Users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtill {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Retrieves the secret key used for signing JWT tokens.
     * The secret key is obtained from the application properties and is converted to a SecretKey object using the HMAC SHA algorithm.
     *
     * @return The SecretKey used for signing JWT tokens.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a JWT access token for the given user and source.
     * The access token contains the user's mobile number as the subject, their role, and the source of the token. The token is set to expire after a short duration (e.g., 20 seconds) and is signed using the secret key.
     */
    public String generateAccessToken(Users user, String source) {
        boolean isGuest = (user == null);
        String subject = isGuest ? "GUEST" : String.valueOf(user.getId());

        return Jwts.builder()
                .setIssuer("stylo-app")
                .setSubject(subject)
                .claim("role", isGuest ? "GUEST" : user.getRole())
                .claim("isGuest", isGuest)
                .claim("source",source)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Generates a JWT refresh token for the given user and source.
     * The refresh token contains the user's mobile number as the subject, their role, and the source of the token. The token is set to expire after a short duration (e.g., 60 seconds) and is signed using the secret key.
     */
    public String generateRefreshToken(Users user, String source) {
        boolean isGuest = (user == null);
        String subject = isGuest ? "GUEST" : String.valueOf(user.getId());

        return Jwts.builder()
                .setIssuer("stylo-app")
                .setSubject(subject)
                .claim("role", isGuest ? "GUEST" : user.getRole())
                .claim("isGuest", isGuest)
                .claim("source",source)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isGuestToken(String token) {
        return "GUEST".equals(
                extractAllClaims(token).get("role", String.class)
        );
    }

    public String getSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // verifies signature
            return !isTokenExpired(token);
        } catch (Exception ex) {
            throw new JwtException("Invalid or expired JWT token");
        }
    }
}
