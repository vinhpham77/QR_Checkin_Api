package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.Device;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration refreshTokenTtl;

    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate,
                               @Value("${app.security.refresh-token.ttl}") int ttl) {

        this.redisTemplate = redisTemplate;
        this.refreshTokenTtl = Duration.ofDays(ttl);
    }

    @Transactional
    public void save(String deviceId, String refreshToken) {
        redisTemplate.opsForValue().set(deviceId, refreshToken, refreshTokenTtl);
    }

    public Optional<String> get(String deviceId) {
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(deviceId));
    }

    @Transactional
    public void delete(String deviceId) {
        redisTemplate.delete(deviceId);
    }

}
