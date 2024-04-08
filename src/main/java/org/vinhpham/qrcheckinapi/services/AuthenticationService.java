package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.common.Utils;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.JWT;
import org.vinhpham.qrcheckinapi.dtos.Login;
import org.vinhpham.qrcheckinapi.dtos.UserDto;
import org.vinhpham.qrcheckinapi.entities.Device;
import org.vinhpham.qrcheckinapi.entities.User;
import org.vinhpham.qrcheckinapi.repositories.UserRepository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.vinhpham.qrcheckinapi.common.Constants.IS_ACCESS_TOKEN;
import static org.vinhpham.qrcheckinapi.common.Constants.IS_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Transactional
    public User register(UserDto request) {
        String username = request.getUsername();
        String email = request.getEmail();

        Optional<User> userOptional = userRepository.findById(username);
        String message;

        if (userOptional.isPresent()) {
            message = Utils.getMessage(messageSource, "error.username.exists");
            throw HandleException.bad(message);
        }

        Optional<User> emailOptional = userRepository.findByEmail(email);

        if (emailOptional.isPresent()) {
            message = Utils.getMessage(messageSource, "error.email.exists");
            throw HandleException.bad(message);
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
            String message = Utils.getMessage(messageSource, "error.username.password.invalid");
            throw new HandleException(message, HttpStatus.UNAUTHORIZED);
        }

        User user = (User) authentication.getPrincipal();
        Set<Device> devices = user.getDevices();
        Device device;

        if (deviceId == null || deviceId.isBlank()) {
            device = Device.builder()
                    .deviceId(deviceId)
                    .deviceName(deviceName)
                    .build();
        } else {
            device = deviceService.findById(deviceId)
                    .orElse(Device.builder()
                            .deviceId(deviceId)
                            .deviceName(deviceName)
                            .build());

            device.setDeviceName(deviceName);
            device.setUpdatedAt(new Date());
        }

        devices.add(device);

        if (!user.getDevices().equals(devices)) {
            deviceService.save(device);
            user.setDevices(devices);
            userRepository.save(user);
        }

        return getJWTs(user, deviceId);
    }

    @Transactional
    public JWT refreshToken(String refreshToken, String deviceId) {
        Optional<String> jwt = refreshTokenService.get(deviceId);

        if (jwt.isEmpty() || !refreshToken.equals(jwt.get())) {
            String message = Utils.getMessage(messageSource, "error.jwt.session.expired");
            throw new HandleException(message, HttpStatus.PRECONDITION_FAILED);
        }

        Optional<Device> device;

        if (deviceId == null || deviceId.isBlank()) {
            throw new HandleException("error.auth.wrong", HttpStatus.NOT_ACCEPTABLE);
        } else {
            device = deviceService.findById(deviceId);
        }

        if (device.isEmpty()) {
            throw new HandleException("error.auth.wrong", HttpStatus.NOT_ACCEPTABLE);
        }

        User user = getAuthUser(refreshToken);
        return getJWTs(user, deviceId);
    }

    private User getAuthUser(String refreshToken) {
        String username = jwtService.extractUserName(refreshToken, IS_REFRESH_TOKEN);

        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    String message = Utils.getMessage(messageSource, "error.auth.wrong");
                    return new HandleException(message, HttpStatus.NOT_ACCEPTABLE);
                });
    }

    private JWT getJWTs(User user, String deviceId) {
        String refreshToken = jwtService.generateToken(user, IS_REFRESH_TOKEN);
        String accessToken = jwtService.generateToken(user, IS_ACCESS_TOKEN);

        refreshTokenService.save(deviceId, refreshToken);

        return new JWT(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String deviceId, String apply, String refreshToken) {
        var user = getAuthUser(refreshToken);

        String storedRefreshToken = refreshTokenService.get(deviceId)
                .orElseThrow(() -> {
                    String message = Utils.getMessage(messageSource, "error.jwt.session.expired");
                    return new HandleException(message, HttpStatus.PRECONDITION_FAILED);
                });

        if (!refreshToken.equals(storedRefreshToken)) {
            String message = Utils.getMessage(messageSource, "error.jwt.session.expired");
            throw new HandleException(message, HttpStatus.PRECONDITION_FAILED);
        }

        if (apply != null && apply.equals("all")) {
            Set<Device> devices = user.getDevices();
            deviceService.deleteMany(devices);
            refreshTokenService.deleteMany(devices);
        } else {
            deviceService.deleteById(deviceId);
            refreshTokenService.delete(deviceId);
        }
    }

}
