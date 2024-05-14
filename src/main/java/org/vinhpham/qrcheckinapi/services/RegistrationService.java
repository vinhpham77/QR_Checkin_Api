package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.entities.Registration;
import org.vinhpham.qrcheckinapi.repositories.RegistrationRepository;

import java.util.Date;
import java.util.List;

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

        if (event.getRegisRequired()) {
            if (slots != null && slots <= 0) {
                throw new HandleException("error.event.full", HttpStatus.BAD_REQUEST);
            } else if (slots != null) {
                event.setSlots(slots - 1);
                eventService.save(event);
            }
        }

        var approval = event.getApprovalRequired();

        var registration = Registration.builder()
                .username(username)
                .event(event)
                .acceptedAt(approval ? null : now)
                .build();

        registrationRepository.save(registration);
    }

    public Registration findByEventIdAndUsername(Long eventId, String username) {
        return registrationRepository.findByEventIdAndUsername(eventId, username).orElse(null);
    }

    public List<Registration> findByUsername(String username, Pageable pageable) {
        return registrationRepository.findByUsername(username, pageable);
    }

    public EventDto checkRegistration(Long id) {
        Event event = eventService.get(id);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        var registration = registrationRepository.findByUsernameAndEventId(username, id).orElse(null);

        return EventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .location(event.getLocation())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .radius(event.getRadius())
                .createdBy(event.getCreatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .updatedBy(event.getUpdatedBy())
                .isRegistered(registration != null && registration.getAcceptedAt() != null)
                .slots(event.getSlots() == null ? null : Math.toIntExact(event.getSlots()))
                .isTicketSeller(event.getIsTicketSeller())
                .regisRequired(event.getRegisRequired())
                .approvalRequired(event.getApprovalRequired())
                .captureRequired(event.getCaptureRequired())
                .backgroundUrl(event.getBackgroundUrl())
                .build();

    }
}
