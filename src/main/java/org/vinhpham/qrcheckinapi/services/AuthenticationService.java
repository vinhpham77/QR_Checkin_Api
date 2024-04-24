package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.JWT;
import org.vinhpham.qrcheckinapi.dtos.Login;
import org.vinhpham.qrcheckinapi.dtos.UserDto;
import org.vinhpham.qrcheckinapi.entities.Device;
import org.vinhpham.qrcheckinapi.entities.User;
import org.vinhpham.qrcheckinapi.repositories.UserRepository;

import java.util.Optional;

import static org.vinhpham.qrcheckinapi.common.Constants.IS_ACCESS_TOKEN;
import static org.vinhpham.qrcheckinapi.common.Constants.IS_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(UserDto request) {
        String username = request.getUsername();
        String email = request.getEmail();

        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isPresent()) {
            throw HandleException.bad("error.username.exists");
        }

        Optional<User> emailOptional = userRepository.findByEmail(email);

        if (emailOptional.isPresent()) {
            throw HandleException.bad("error.email.exists");
        }

        String hashPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(username)
                .hashPassword(hashPassword)
                .email(request.getEmail())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public JWT login(Login request, String deviceId, String deviceName) {
        String username = request.getUsername();
        String password = request.getPassword();
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            throw new HandleException("error.username.password.invalid", HttpStatus.UNAUTHORIZED);
        }

        User user = (User) authentication.getPrincipal();
        Device device = deviceService.findByUser(user).orElse(null);

        if (device == null) {
            device = Device.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .user(user)
                    .build();
        } else {
            User deviceUser = device.getUser();
            if (!device.getDeviceId().equals(deviceId) || (deviceUser != null && !deviceUser.equals(user))) {
                throw new HandleException("error.device.exists", HttpStatus.UNAUTHORIZED);
            }
        }

        device.setUser(user);
        device.setUpdatedAt(null);

        return getJWTs(user, device);
    }

    @Transactional
    public JWT refreshToken(String refreshToken, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new HandleException("error.auth.wrong", HttpStatus.NOT_ACCEPTABLE);
        }

        Device device = deviceService.findById(deviceId).orElseThrow(
                () -> new HandleException("error.auth.wrong", HttpStatus.NOT_FOUND));
        String jwt = device.getRefreshToken();

        if (!refreshToken.equals(jwt)) {
            throw new HandleException("error.jwt.session.expired", HttpStatus.NOT_ACCEPTABLE);
        }

        User user = getAuthUser(refreshToken);
        String username = user.getUsername();
        User deviceUser = device.getUser();
        String deviceUsername = deviceUser == null ? null : deviceUser.getUsername();

        if (deviceUser != null && !username.equals(deviceUsername)) {
            throw new HandleException("error.auth.wrong", HttpStatus.NOT_ACCEPTABLE);
        }

        return getJWTs(user, device);
    }

    private User getAuthUser(String refreshToken) {
        String username = jwtService.extractUserName(refreshToken, IS_REFRESH_TOKEN);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new HandleException("error.auth.wrong", HttpStatus.NOT_ACCEPTABLE));
    }

    private JWT getJWTs(User user, Device device) {
        String refreshToken = jwtService.generateToken(user, IS_REFRESH_TOKEN);
        String accessToken = jwtService.generateToken(user, IS_ACCESS_TOKEN);

        device.setRefreshToken(refreshToken);
        deviceService.save(device);

        return new JWT(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String deviceId, String refreshToken) {
        var user = getAuthUser(refreshToken);

        Device device = deviceService.findById(deviceId).orElse(null);
        User userDevice = device == null ? null : device.getUser();
        String storedRefreshToken = device == null ? null : device.getRefreshToken();

        if (user.equals(userDevice) && refreshToken.equals(storedRefreshToken)) {
            device.setRefreshToken(null);
            deviceService.save(device);
        } else {
            throw new HandleException("error.jwt.session.expired", HttpStatus.PRECONDITION_FAILED);

        }
    }
}
