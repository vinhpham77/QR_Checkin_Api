package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.ItemCounter;
import org.vinhpham.qrcheckinapi.dtos.RegistrationUser;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.entities.Registration;
import org.vinhpham.qrcheckinapi.repositories.RegistrationRepository;

import java.util.Date;

import static org.vinhpham.qrcheckinapi.utils.Utils.getPageable;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final UserService userService;

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

    public Page<Registration> findByUsername(String username, Pageable pageable) {
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
                .backgroundImage(event.getBackgroundImage())
                .build();
    }

    public ItemCounter<RegistrationUser> getRegistrationUsers(Long eventId, Integer page, int size, boolean isPending) {
        Pageable pageable = getPageable(page, size, "createdAt", true);

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var event = eventService.get(eventId);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        if (!event.getCreatedBy().equals(username)) {
            throw new HandleException("error.event.user.not.permitted", HttpStatus.FORBIDDEN);
        }

        var registrations = isPending ? registrationRepository.findByEventIdAndAcceptedAtIsNull(eventId, pageable) :
                registrationRepository.findByEventIdAndAcceptedAtIsNotNull(eventId, pageable);

        var total = registrations.getTotalElements();
        var registrationDetails = registrations.getContent().stream().map(registration -> {
            var user = userService.findByUsername(registration.getUsername()).get();

            return RegistrationUser.builder()
                    .registrationId(registration.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .avatar(user.getAvatar())
                    .sex(user.getSex())
                    .email(user.getEmail())
                    .createdAt(registration.getCreatedAt())
                    .acceptedAt(registration.getAcceptedAt())
                    .build();
        }).toList();

        return new ItemCounter<>(registrationDetails, total);

    }

    public void acceptRegistration(Long id) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var registration = registrationRepository.findById(id).orElse(null);

        if (registration == null) {
            throw new HandleException("error.registration.not.found", HttpStatus.NOT_FOUND);
        }

        var event = registration.getEvent();

        if (!event.getCreatedBy().equals(username)) {
            throw new HandleException("error.event.user.not.permitted", HttpStatus.FORBIDDEN);
        }

        if (registration.getAcceptedAt() != null) {
            throw new HandleException("error.registration.already.accepted", HttpStatus.BAD_REQUEST);
        }

        registration.setAcceptedAt(new Date());
        registrationRepository.save(registration);
    }

    public void rejectRegistration(Long id) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var registration = registrationRepository.findById(id).orElse(null);

        if (registration == null) {
            throw new HandleException("error.registration.not.found", HttpStatus.NOT_FOUND);
        }

        var event = registration.getEvent();

        if (!event.getCreatedBy().equals(username)) {
            throw new HandleException("error.event.user.not.permitted", HttpStatus.FORBIDDEN);
        }

        if (registration.getAcceptedAt() != null) {
            throw new HandleException("error.registration.already.accepted", HttpStatus.BAD_REQUEST);
        }

        registrationRepository.delete(registration);
    }
}
