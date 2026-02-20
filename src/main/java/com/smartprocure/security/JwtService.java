package com.smartprocure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final String issuer;
    private final String secret;
    private final long accessTokenExpirationMinutes;

    public JwtService(
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes) {
        this.issuer = issuer;
        this.secret = secret;
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
    }

    public String generateAccessToken(SmartProcurePrincipal principal) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        Map<String, Object> claims = Map.of(
                "uid", principal.getId(),
                "role", principal.getRole().name());

        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.getUsername())
                .id(java.util.UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        Claims claims = parseClaims(token);
        String subject = claims.getSubject();
        Date exp = claims.getExpiration();
        return expectedSubject.equals(subject) && exp != null && exp.after(new Date());
    }

    private SecretKey getSigningKey() {
        // Prefer base64 secret if provided; fall back to raw string bytes.
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (Exception ex) {
            // Fallback to using the secret as raw bytes if it's not Base64 encoded
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
