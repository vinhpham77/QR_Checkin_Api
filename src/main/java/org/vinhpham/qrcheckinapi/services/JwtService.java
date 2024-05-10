package org.vinhpham.qrcheckinapi.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${token.secret.key}")
    private String jwtSecretKey;

    @Value("${token.expirationms}")
    private Long jwtExpirationMs;

    @Value("${token.refresh.secret.key}")
    private String jwtRefreshSecretKey;

    @Value("${token.refresh.expirationms}")
    Long jwtRefreshExpirationMs;

    @Getter
    private Key signingKey;

    @Getter
    private Key refreshSigningKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    public void initRefresh() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtRefreshSecretKey);
        this.refreshSigningKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getSubject, isRefreshToken);
    }

    public String generateToken(UserDetails userDetails, boolean isRefreshToken) {
        User user = (User) userDetails;
        Map<String, Object> claims = setBasicClaims(user);
        return generateToken(claims, userDetails, isRefreshToken);
    }

    public String generateQrToken(Long eventId, String eventSecretKey) {
        byte[] keyBytes = (jwtSecretKey + eventSecretKey).getBytes();

        var qrSigningKey = Keys.hmacShaKeyFor(keyBytes);
        var claims = new HashMap<String, Object>();
        claims.put("eventId", eventId);
        var thirtySecondsInMs = 30000L;

        return Jwts
                .builder()
                .setClaims(claims)
                .signWith(qrSigningKey, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + thirtySecondsInMs))
                .compact();
    }

    public Map<String, Object> setBasicClaims(User user) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", user.getRole());
        claims.put("status", user.getStatus());
        claims.put("fullName", user.getFullName());

        return claims;
    }

    public boolean isTokenValid(String token, UserDetails userDetails, boolean isRefreshToken) {
        return !isTokenExpired(token, isRefreshToken)
                &&
                extractUserName(token, isRefreshToken).equals(userDetails.getUsername());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers, boolean isRefreshToken) {
        final Claims claims = extractAllClaims(token, isRefreshToken);
        return claimsResolvers.apply(claims);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, boolean isRefreshToken) {
        Key signingKey = isRefreshToken ? getRefreshSigningKey() : getSigningKey();
        var expirationMs = isRefreshToken ? jwtRefreshExpirationMs : jwtExpirationMs;
        var now = new Date(System.currentTimeMillis());
        var expiration = new Date(System.currentTimeMillis() + expirationMs);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractExpiration(token, isRefreshToken).before(new Date());
    }

    public Date extractExpiration(String token, boolean isRefreshToken) {
        return extractClaim(token, Claims::getExpiration, isRefreshToken);
    }

    public Claims extractAllClaims(String token, boolean isRefreshToken) {
        Key signingKey = isRefreshToken ? getRefreshSigningKey() : getSigningKey();

        return Jwts
                .parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
