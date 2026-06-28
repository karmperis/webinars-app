package com.karmperis.webinarsapp.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Service for generating and validating JWTs and extracting token claims.
 */
@Service
public class JwtService {
    @Value("${app.security.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt-expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT for the given username role and capabilities.
     *
     * @param username     the subject of the token
     * @param role         the role claim to include
     * @param uuid         the user UUID claim to include
     * @param capabilities the capability names to include
     * @return the signed JWT
     */
    public String generateToken(String username, String role, String uuid, List<String> capabilities) {
        var claims = new HashMap<String, Object>();
        claims.put("role", role);
        claims.put("uuid", uuid);
        claims.put("capabilities", capabilities);
        return Jwts
                .builder()
                .issuer("https://api.webinarsapp.com")
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validate the token against the provided user details.
     *
     * @param token       the JWT to validate
     * @param userDetails the user details to compare against
     * @return {@code true} if the token is valid and not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subject = extractSubject(token);
        return (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Extract the subject (username) from the token.
     *
     * @param token the JWT
     * @return the token subject
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract a claim using the provided resolver.
     *
     * @param token          the JWT
     * @param claimsResolver function that maps claims to a value
     * @param <T>            return type of the claim
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Check whether the token has expired.
     *
     * @param token the JWT
     * @return {@code true} if the token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract the expiration date from the token.
     *
     * @param token the JWT
     * @return the expiration {@link Date}
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from the token.
     *
     * @param token the JWT
     * @return the token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Creates a HS256 Key. Key is an interface.
     * Starting from secretKey we get a byte array
     * of the secret. Then we get the {@link javax.crypto.SecretKey},
     * class that implements the {@link Key} interface.
     *
     * @return a SecretKey which implements Key.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}