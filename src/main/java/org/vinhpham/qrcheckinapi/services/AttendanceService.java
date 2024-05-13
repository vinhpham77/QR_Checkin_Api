package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.ItemCounter;
import org.vinhpham.qrcheckinapi.dtos.RegistrationDetail;
import org.vinhpham.qrcheckinapi.entities.Attendance;
import org.vinhpham.qrcheckinapi.repositories.AttendanceRepository;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final JwtService jwtService;
    private final EventService eventService;
    private final RegistrationService registrationService;

    @Transactional
    public void checkIn(String code, Long eventId) {
        var event = eventService.get(eventId);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var slots = event.getSlots();

        if (!event.getRegisRequired()) {
            if (slots != null && slots <= 0) {
                throw new HandleException("error.event.full", HttpStatus.BAD_REQUEST);
            } else if (slots != null) {
                event.setSlots(slots - 1);
                eventService.save(event);
            }
        } else {
            var registration = registrationService.findByEventIdAndUsername(eventId, username);

            if (registration == null) {
                throw new HandleException("error.not.registered", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public Attendance findByRegistrationId(Long registrationId) {
        return attendanceRepository.findByRegistrationId(registrationId);
    }

    public ItemCounter<RegistrationDetail> getRegistrations(Integer page, int size) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (page == null || page < 1) {
            page = 1;
        }

        page--;

        if (size < 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        var registrations = registrationService.findByUsername(username, pageable);

        var total = registrations.size();
        var registrationDetails = registrations.stream().map(registration -> {
            var event = registration.getEvent();
            var attendance = findByRegistrationId(registration.getId());

            return RegistrationDetail.builder()
                    .id(registration.getId())
                    .eventName(event.getName())
                    .checkInAt(attendance != null ? attendance.getCheckInAt() : null)
                    .checkOutAt(attendance != null ? attendance.getCheckOutAt() : null)
                    .createdAt(registration.getCreatedAt())
                    .acceptedAt(registration.getAcceptedAt())
                    .eventCreator(event.getCreatedBy())
                    .eventLocation(event.getLocation())
                    .build();
        }).toList();

        return new ItemCounter<>(registrationDetails, total);

    }
}
