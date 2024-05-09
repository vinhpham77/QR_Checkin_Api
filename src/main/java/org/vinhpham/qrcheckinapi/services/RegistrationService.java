package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.entities.Registration;
import org.vinhpham.qrcheckinapi.repositories.RegistrationRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;

    @Transactional
    public void register(Long eventId) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        registrationRepository.findByUsernameAndEventId(username, eventId).ifPresent(registration -> {
            throw new HandleException("error.already.registered", HttpStatus.BAD_REQUEST);
        });

        var event = eventService.get(eventId);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        var now = new Date();

        if (event.getEndAt().before(now)) {
            throw new HandleException("error.event.ended", HttpStatus.BAD_REQUEST);
        }

        var slots = event.getSlots();

        if (slots != null && slots <= 0) {
            throw new HandleException("error.event.full", HttpStatus.BAD_REQUEST);
        } else if (slots != null) {
            event.setSlots(slots - 1);
            eventService.save(event);
        }

        var approval = event.getApprovalRequired();

        var registration = Registration.builder()
                .username(username)
                .event(event)
                .acceptedAt(approval ? null : now)
                .build();

        registrationRepository.save(registration);
    }
}
