package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vinhpham.qrcheckinapi.dtos.RegistrationRequest;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.services.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        registrationService.register(registrationRequest.getEventId());
        return Success.ok(null);
    }
}
