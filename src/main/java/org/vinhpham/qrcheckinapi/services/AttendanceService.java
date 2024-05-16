package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.ItemCounter;
import org.vinhpham.qrcheckinapi.dtos.RegistrationDetail;
import org.vinhpham.qrcheckinapi.entities.Attendance;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.entities.Registration;
import org.vinhpham.qrcheckinapi.repositories.AttendanceRepository;

import java.util.Date;

import static org.vinhpham.qrcheckinapi.utils.Utils.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final JwtService jwtService;
    private final EventService eventService;
    private final ImageService imageService;
    private final RegistrationService registrationService;

    public Attendance findByRegistrationId(Long registrationId) {
        return attendanceRepository.findByRegistrationId(registrationId);
    }

    public ItemCounter<RegistrationDetail> getRegistrations(Integer page, int size) {
        Pageable pageable = getCreatedAtPageable(page, size);

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
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

    @Transactional
    public void checkIn(Long id, MultipartFile qrImage, MultipartFile portraitImage, String code, String latitude, String longitude) {
        var event = eventService.get(id);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        jwtService.verifyQrToken(code, event.getId(), event.getCheckinQrCode());
        var slots = event.getSlots();
        var now = new Date();
        Registration registration;

        if (event.getEndAt().before(now)) {
            throw new HandleException("error.event.ended", HttpStatus.BAD_REQUEST);
        }

        validateTimeAndLocation(portraitImage, latitude, longitude, event, now);

        if (event.getRegisRequired()) {
            registration = registrationService.findByEventIdAndUsername(id, username);

            if (registration == null) {
                throw new HandleException("error.not.registered", HttpStatus.BAD_REQUEST);
            } else if (registration.getAcceptedAt() == null) {
                throw new HandleException("error.not.accepted", HttpStatus.BAD_REQUEST);
            }
        }

        String qrImageName = imageService.upload(qrImage);
        String portraitImageName = null;

        if (portraitImage != null) {
            portraitImageName = imageService.upload(portraitImage);
        }

        Attendance attendance = attendanceRepository.findByUsernameAndEventId(username, id);
        String oldPortraitImageName = null;
        String oldQrImageName = null;

        if (attendance == null) {
            if (!event.getRegisRequired()) {
                if (slots != null && slots <= 0) {
                    throw new HandleException("error.event.full", HttpStatus.BAD_REQUEST);
                } else if (slots != null) {
                    event.setSlots(slots - 1);
                    eventService.save(event);
                }
            }

            attendance = Attendance.builder()
                    .checkInImg(portraitImageName)
                    .registrationId(null)
                    .qrCheckInImg(qrImageName)
                    .username(username)
                    .eventId(event.getId())
                    .checkInAt(now)
                    .build();
        } else {
            oldPortraitImageName = attendance.getCheckInImg();
            oldQrImageName = attendance.getQrCheckInImg();

            attendance.setCheckInImg(portraitImageName);
            attendance.setQrCheckInImg(qrImageName);
            attendance.setCheckInAt(now);
        }

        imageService.saveByName(qrImageName);

        if (portraitImageName != null) {
            imageService.saveByName(portraitImageName);
        }

        attendanceRepository.save(attendance);

        if (oldPortraitImageName != null) {
            imageService.deleteByName(oldPortraitImageName);
        }

        if (oldQrImageName != null) {
            imageService.deleteByName(oldQrImageName);
        }
    }

    private void validateTimeAndLocation(MultipartFile portraitImage, String latitude, String longitude, Event event, Date now) {
        if (event.getStartAt().after(now)) {
            throw new HandleException("error.event.not.started", HttpStatus.BAD_REQUEST);
        }

        if (event.getCaptureRequired() && portraitImage == null) {
            throw new HandleException("error.portrait.required", HttpStatus.BAD_REQUEST);
        }

        double userLatitude = Double.parseDouble(latitude);
        double userLongitude = Double.parseDouble(longitude);
        double eventLatitude = toDouble(event.getLatitude());
        double eventLongitude = toDouble(event.getLongitude());
        double eventRadius = event.getRadius();

        if (!isInEventRadius(userLatitude, userLongitude, eventLatitude, eventLongitude, eventRadius)) {
            throw new HandleException("error.not.in.event.radius", HttpStatus.BAD_REQUEST, eventRadius);
        }
    }

    public void checkOut(Long id, MultipartFile qrImage, MultipartFile portraitImage, String code, String latitude, String longitude) {
        var event = eventService.get(id);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        jwtService.verifyQrToken(code, event.getId(), event.getCheckoutQrCode());
        var now = new Date();
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var attendance = attendanceRepository.findByUsernameAndEventId(username, id);

        if (attendance == null) {
            throw new HandleException("error.not.checked.in", HttpStatus.BAD_REQUEST);
        }

        if (attendance.getCheckOutAt() != null) {
            throw new HandleException("error.already.checked.out", HttpStatus.BAD_REQUEST);
        }

        var endAt = new Date(event.getEndAt().getTime() + 3600000);
        if (endAt.before(now)) {
            throw new HandleException("error.event.ended.1h", HttpStatus.BAD_REQUEST);
        }

        validateTimeAndLocation(portraitImage, latitude, longitude, event, now);

        String qrImageName = imageService.upload(qrImage);
        String portraitImageName = null;

        if (portraitImage != null) {
            portraitImageName = imageService.upload(portraitImage);
        }

        attendance.setCheckOutImg(portraitImageName);
        attendance.setQrCheckOutImg(qrImageName);
        attendance.setCheckOutAt(now);

        attendanceRepository.save(attendance);
        imageService.saveByName(qrImageName);
        if (portraitImageName != null) {
            imageService.saveByName(portraitImageName);
        }
    }
}
