package org.vinhpham.qrcheckinapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vinhpham.qrcheckinapi.common.Constants;
import org.vinhpham.qrcheckinapi.dtos.*;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.services.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Event event = eventService.get(id);
        return Success.ok(event);
    }

    @GetMapping
    public ResponseEntity<?> getEvents(@RequestParam(required = false, name = "fields") List<String> fields,
                                       @RequestParam(required = false, name = "keyword") String keyword,
                                       @RequestParam(required = false, name = "sortField", defaultValue = "") String sortField,
                                       @RequestParam(required = false, name = "categoryId", defaultValue = "") Integer category,
                                       @RequestParam(required = false, name = "isAsc", defaultValue = "true") Boolean isAsc,
                                       @RequestParam(required = false, name = "page") Integer page,
                                       @RequestParam(required = false, name = "limit", defaultValue = "10") int limit,
                                       HttpServletRequest request) {

        EventSearchCriteria searchCriteria = new EventSearchCriteria();
        searchCriteria.setKeyword(keyword);
        searchCriteria.setFields(fields);
        searchCriteria.setSortField(sortField);
        searchCriteria.setCategoryId(category);
        searchCriteria.setIsAsc(isAsc);
        searchCriteria.setPage(page);
        searchCriteria.setLimit(limit);

        String latitude = request.getHeader(Constants.KEY_LATITUDE);
        String longitude = request.getHeader(Constants.KEY_LONGITUDE);

        ItemCounter<EventDto> events = eventService.get(searchCriteria, latitude, longitude);
        return Success.ok(events);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EventDto event) {
        Event newEvent = eventService.create(event);
        return Success.ok(newEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EventDto event) {
        Event updatedEvent = eventService.update(id, event);
        return Success.ok(updatedEvent);
    }

    @PostMapping("/generate-qr")
    public ResponseEntity<?> generateQrCode(@RequestBody GenerateQrRequest request) {
        String jwt = eventService.generateQrCode(request);
        return Success.ok(jwt);
    }
}
