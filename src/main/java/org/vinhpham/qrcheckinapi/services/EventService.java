package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.repositories.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;

    public Event get(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new HandleException("error.event.not.found", HttpStatus.NOT_FOUND)
        );
    }

    @Transactional
    public Event create(EventDto eventDto) {
        String requester = SecurityContextHolder.getContext().getAuthentication().getName();

        Event event = Event.builder()
                .name(eventDto.getName())
                .description(eventDto.getDescription())
                .startAt(eventDto.getStartAt())
                .endAt(eventDto.getEndAt())
                .categories(eventDto.getCategories())
                .createdBy(requester)
                .updatedBy(requester)
                .backgroundUrl(eventDto.getBackgroundUrl())
                .slots(eventDto.getSlots())
                .location(eventDto.getLocation())
                .latitude(eventDto.getLatitude())
                .longitude(eventDto.getLongitude())
                .radius(eventDto.getRadius())
                .approvalRequired(eventDto.getApprovalRequired())
                .regisRequired(eventDto.getRegisRequired())
                .checkoutQrCode(eventDto.getCheckoutQrCode())
                .captureRequired(eventDto.getCaptureRequired())
                .build();

        String backgroundUrl = eventDto.getBackgroundUrl();
        if (backgroundUrl != null && !backgroundUrl.isBlank()) {
            event.setBackgroundUrl(backgroundUrl);
            imageService.saveByUrl(backgroundUrl);
        }

        return eventRepository.save(event);
    }

    public Event update(Long id, EventDto eventDto) {
        Event event = get(id);

        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setStartAt(eventDto.getStartAt());
        event.setEndAt(eventDto.getEndAt());
        event.setCategories(eventDto.getCategories());
        event.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        event.setBackgroundUrl(eventDto.getBackgroundUrl());
        event.setSlots(eventDto.getSlots());
        event.setLocation(eventDto.getLocation());
        event.setLatitude(eventDto.getLatitude());
        event.setLongitude(eventDto.getLongitude());
        event.setRadius(eventDto.getRadius());
        event.setApprovalRequired(eventDto.getApprovalRequired());
        event.setRegisRequired(eventDto.getRegisRequired());
        event.setCheckoutQrCode(eventDto.getCheckoutQrCode());
        event.setCaptureRequired(eventDto.getCaptureRequired());

        String backgroundUrl = eventDto.getBackgroundUrl();
        if (backgroundUrl != null && !backgroundUrl.isBlank()) {
            imageService.deleteByUrl(event.getBackgroundUrl());
            imageService.saveByUrl(backgroundUrl);
            event.setBackgroundUrl(backgroundUrl);
        }

        return eventRepository.save(event);
    }
}
