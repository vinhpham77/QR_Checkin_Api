package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.dtos.RegistrationRequest;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.services.AttendanceService;
import org.vinhpham.qrcheckinapi.services.RegistrationService;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final AttendanceService attendanceService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest registrationRequest) {
        registrationService.register(registrationRequest.getEventId());
        return Success.ok(null);
    }

    @GetMapping("/{id}/check")
    public ResponseEntity<?> checkRegistration(@PathVariable Long id) {
        EventDto event = registrationService.checkRegistration(id);
        return Success.ok(event);
    }

    @GetMapping
    public ResponseEntity<?> getRegistrations(@RequestParam(required = false, name = "page") Integer page,
                                              @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        var items = attendanceService.getRegistrations(page, size);
        return Success.ok(items);
    }

    @GetMapping("/{id}/pending")
    public ResponseEntity<?> getPendingRegistrations(@PathVariable Long id, @RequestParam(required = false, name = "page") Integer page,
                                                     @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        var items = registrationService.getRegistrationUsers(id, page, size, true);
        return Success.ok(items);
    }

    @GetMapping("/{id}/accepted")
    public ResponseEntity<?> getAcceptedRegistrations(@PathVariable Long id, @RequestParam(required = false, name = "page") Integer page,
                                                      @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        var items = registrationService.getRegistrationUsers(id, page, size, false);
        return Success.ok(items);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptRegistration(@PathVariable Long id) {
        registrationService.acceptRegistration(id);
        return Success.ok(null);
    }

    @DeleteMapping("/{id}/reject")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long id) {
        registrationService.rejectRegistration(id);
        return Success.ok(null);
    }

}
