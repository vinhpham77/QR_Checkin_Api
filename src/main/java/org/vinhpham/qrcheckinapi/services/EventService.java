package org.vinhpham.qrcheckinapi.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.*;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.entities.TicketType;
import org.vinhpham.qrcheckinapi.repositories.EventRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;
    private final EntityManager entityManager;
    private final TicketTypeService ticketTypeService;
    private final JwtService jwtService;

    @Value("#{'${event.search.fields}'.split(',')}")
    private List<String> searchFields;

    @Value("#{'${event.sort.fields}'.split(',')}")
    private List<String> sortFields;

    public Event get(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new HandleException("error.event.not.found", HttpStatus.NOT_FOUND)
        );
    }

    public ItemCounter<EventDto> get(EventSearchCriteria searchCriteria, String latitude, String longitude) {
        String sortField = searchCriteria.getSortField();
        Integer page = searchCriteria.getPage();
        List<String> fields = searchCriteria.getFields();
        Integer limit = searchCriteria.getLimit();

        if (sortField == null || sortField.isBlank()) {
            sortField = "created_at";
        }

        if (!sortFields.contains(sortField)) {
            throw new HandleException("event.sort.field.invalid", HttpStatus.BAD_REQUEST);
        }

        if (page == null || page < 0) {
            searchCriteria.setPage(0);
        } else {
            searchCriteria.setPage(page - 1);
        }

        if (limit == null || limit < 0) {
            searchCriteria.setLimit(10);
        }

        if (fields != null && new HashSet<>(searchFields).containsAll(fields)) {
            searchCriteria.setFields(fields);
        } else {
            searchCriteria.setFields(searchFields);
        }

        return findByFields(searchCriteria, latitude, longitude);
    }

    private ItemCounter<EventDto> findByFields(EventSearchCriteria searchCriteria, String latitude, String longitude) {
        StringBuilder searchQueryString = new StringBuilder("SELECT events.*, " +
                "ST_Distance_Sphere(point(longitude, latitude), point(:longitude, :latitude)) " +
                "as distance " +
                "FROM events LEFT JOIN event_categories ON events.id = event_categories.event_id " +
                "LEFT JOIN categories ON event_categories.category_id = categories.id " +
                "WHERE TRUE");

        var fields = searchCriteria.getFields();
        var keyword = searchCriteria.getKeyword();
        var categoryId = searchCriteria.getCategoryId();
        var sortField = searchCriteria.getSortField();
        var direction = searchCriteria.getIsAsc() ? "ASC" : "DESC";
        var limit = searchCriteria.getLimit();
        var offset = searchCriteria.getPage() * limit;

        if (categoryId != null) {
            searchQueryString.append(" AND categories.id = :categoryId");
        }

        if (keyword != null && !keyword.isBlank()) {
            searchQueryString.append(" AND (FALSE");
            for (String field : fields) {
                searchQueryString.append(" OR events.").append(field).append(" LIKE :keyword");
            }
            searchQueryString.append(")");
        }

        searchQueryString.append(" GROUP BY events.id");
        if (sortField != null && !sortField.isBlank()) {
            if (sortField.equals("distance")) {
                searchQueryString.append(" ORDER BY distance ").append(direction);
            } else {
                searchQueryString.append(" ORDER BY events.").append(sortField).append(" ").append(direction);
            }
        }
        String countString = searchQueryString.toString();
        searchQueryString.append(" LIMIT :limit OFFSET :offset");

        Query searchQuery = entityManager.createNativeQuery(searchQueryString.toString());
        searchQuery.setParameter("latitude", latitude);
        searchQuery.setParameter("longitude", longitude);
        if (categoryId != null) {
            searchQuery.setParameter("categoryId", categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            searchQuery.setParameter("keyword", "%" + keyword + "%");
        }
        searchQuery.setParameter("limit", limit);
        searchQuery.setParameter("offset", offset);

        Query countQuery = entityManager.createNativeQuery(countString);
        countQuery.setParameter("latitude", latitude);
        countQuery.setParameter("longitude", longitude);
        if (keyword != null && !keyword.isBlank()) {
            countQuery.setParameter("keyword", "%" + keyword + "%");
        }
        if (categoryId != null) {
            countQuery.setParameter("categoryId", categoryId);
        }

        List<Object[]> results = searchQuery.getResultList();
        List<EventDto> events = new ArrayList<>();

        for (Object[] result : results) {
            EventDto eventDto = new EventDto();
            eventDto.setId((Long) result[0]);
            eventDto.setName((String) result[1]);
            eventDto.setBackgroundImage((String) result[2]);
            eventDto.setDescription((String) result[3]);
            eventDto.setSlots((Integer) result[4]);
            eventDto.setStartAt((Date) result[5]);
            eventDto.setEndAt((Date) result[6]);
            eventDto.setLocation((String) result[7]);
            eventDto.setLatitude((BigDecimal) result[8]);
            eventDto.setLongitude((BigDecimal) result[9]);
            eventDto.setRadius((Double) result[10]);
            eventDto.setRegisRequired((Boolean) result[11]);
            eventDto.setApprovalRequired((Boolean) result[12]);
            eventDto.setCaptureRequired((Boolean) result[13]);
            eventDto.setCheckinQrCode((String) result[14]);
            eventDto.setCheckoutQrCode((String) result[15]);
            eventDto.setCreatedAt((Date) result[16]);
            eventDto.setCreatedBy((String) result[17]);
            eventDto.setUpdatedAt((Date) result[18]);
            eventDto.setUpdatedBy((String) result[19]);
            eventDto.setIsTicketSeller((Boolean) result[22]);
            eventDto.setDistance((Double) result[23]);
            events.add(eventDto);
        }

        Long counter = (long) countQuery.getResultList().size();

        return new ItemCounter<>(events, counter);
    }

    @Transactional
    public Event create(EventDto eventDto) {
        String requester = SecurityContextHolder.getContext().getAuthentication().getName();

        List<TicketTypeDto> ticketTypes = eventDto.getTicketTypes();
        if (ticketTypes != null) {
            for (TicketTypeDto ticketType : ticketTypes) {
                if (ticketType.getPrice() == null || ticketType.getQuantity() == null) {
                    throw new HandleException("error.ticket.type.invalid", HttpStatus.BAD_REQUEST, ticketType.getName());
                }
            }
        }

        Event event = Event.builder()
                .name(eventDto.getName())
                .description(eventDto.getDescription())
                .startAt(eventDto.getStartAt())
                .endAt(eventDto.getEndAt())
                .categories(eventDto.getCategories())
                .createdBy(requester)
                .updatedBy(requester)
                .isTicketSeller(eventDto.getIsTicketSeller())
                .backgroundImage(eventDto.getBackgroundImage())
                .slots(eventDto.getSlots() == null ? null : Long.valueOf(eventDto.getSlots()))
                .location(eventDto.getLocation())
                .latitude(eventDto.getLatitude())
                .longitude(eventDto.getLongitude())
                .radius(eventDto.getRadius())
                .approvalRequired(eventDto.getApprovalRequired())
                .regisRequired(eventDto.getRegisRequired())
                .checkoutQrCode(eventDto.getCheckoutQrCode())
                .captureRequired(eventDto.getCaptureRequired())
                .build();

        String background = eventDto.getBackgroundImage();
        if (background != null && !background.isBlank()) {
            event.setBackgroundImage(background);
            imageService.saveByName(background);
        }

        Event newEvent = eventRepository.save(event);

        saveTicketType(requester, newEvent, ticketTypes);

        return newEvent;
    }

    @Transactional
    public Event update(Long id, EventDto eventDto) {
        Event event = get(id);

        String requester = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!requester.equals(event.getCreatedBy())) {
            throw new HandleException("error.event.not.authorized", HttpStatus.FORBIDDEN);
        }

        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setStartAt(eventDto.getStartAt());
        event.setEndAt(eventDto.getEndAt());
        event.setCategories(eventDto.getCategories());
        event.setUpdatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        event.setBackgroundImage(eventDto.getBackgroundImage());
        event.setSlots(eventDto.getSlots() == null ? null : Long.valueOf(eventDto.getSlots()));
        event.setLocation(eventDto.getLocation());
        event.setLatitude(eventDto.getLatitude());
        event.setLongitude(eventDto.getLongitude());
        event.setRadius(eventDto.getRadius());
        event.setApprovalRequired(eventDto.getApprovalRequired());
        event.setRegisRequired(eventDto.getRegisRequired());
        event.setCheckoutQrCode(eventDto.getCheckoutQrCode());
        event.setCaptureRequired(eventDto.getCaptureRequired());

        String background = eventDto.getBackgroundImage();
        if (background != null && !background.isBlank()) {
            imageService.deleteByName(event.getBackgroundImage());
            imageService.saveByName(background);
            event.setBackgroundImage(background);
        }

        Event updatedEvent = eventRepository.save(event);

        List<TicketTypeDto> ticketTypes = eventDto.getTicketTypes();
        saveTicketType(requester, updatedEvent, ticketTypes);

        return updatedEvent;
    }

    private void saveTicketType(String requester, Event updatedEvent, List<TicketTypeDto> ticketTypes) {
        if (ticketTypes != null) {
            List<TicketType> ticketTypeList = new ArrayList<>();
            for (TicketTypeDto ticketType : ticketTypes) {
                var id = ticketType.getId();

                if (id == 0) {
                    id = null;
                }

                TicketType newTicketType = TicketType.builder()
                        .id(id)
                        .name(ticketType.getName())
                        .description(ticketType.getDescription())
                        .quantity(ticketType.getQuantity())
                        .price(ticketType.getPrice())
                        .eventId(updatedEvent.getId())
                        .createdBy(requester)
                        .updatedBy(requester)
                        .build();
                ticketTypeList.add(newTicketType);
            }

            var deletedTicketTypes = ticketTypeService.getByEventId(updatedEvent.getId());
            deletedTicketTypes.forEach(ticketType -> {
                if (!ticketTypeList.contains(ticketType)) {
                    ticketTypeService.delete(ticketType);
                }
            });

            ticketTypeService.saveAll(ticketTypeList);
        }
    }

    @Transactional
    public void save(Event event) {
        eventRepository.save(event);
    }

    public String generateQrCode(GenerateQrRequest request) {
        var eventId = request.getEventId();
        var isCheckin = request.getIsCheckIn();
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        var event = get(eventId);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        if (event.getIsTicketSeller()) {
            throw new HandleException("error.event.not.attendance", HttpStatus.BAD_REQUEST);
        }

        if (!username.equals(event.getCreatedBy())) {
            throw new HandleException("error.event.not.authorized", HttpStatus.FORBIDDEN);
        }

        var now = new Date();
        if (isCheckin && event.getStartAt().after(now)) {
            throw new HandleException("error.event.not.started", HttpStatus.BAD_REQUEST);
        }

        if (isCheckin && event.getEndAt().before(now)) {
            throw new HandleException("error.event.ended", HttpStatus.BAD_REQUEST);
        }

        var endAt = new Date(event.getEndAt().getTime() + 3600000);
        if (!isCheckin && endAt.before(now)) {
            throw new HandleException("error.event.ended.1h", HttpStatus.BAD_REQUEST);
        }

        if (isCheckin) {
            return jwtService.generateQrToken(eventId, event.getCheckinQrCode());
        } else {
            return jwtService.generateQrToken(eventId, event.getCheckoutQrCode());
        }
    }
}
