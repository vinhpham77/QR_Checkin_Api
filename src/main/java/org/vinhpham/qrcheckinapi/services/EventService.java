package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.repositories.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;

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
                .isApproved(eventDto.getIsApproved())
                .isRequired(eventDto.getIsRequired())
                .checkoutQrCode(eventDto.getCheckoutQrCode())
                .build();

        String backgroundUrl = eventDto.getBackgroundUrl();
        if (backgroundUrl != null && !backgroundUrl.isBlank()) {
            event.setBackgroundUrl(backgroundUrl);
            imageService.saveByUrl(backgroundUrl);
        }

        return eventRepository.save(event);
    }
}
