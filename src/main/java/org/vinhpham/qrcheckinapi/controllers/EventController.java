package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vinhpham.qrcheckinapi.dtos.EventDto;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.entities.Event;
import org.vinhpham.qrcheckinapi.services.EventService;

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
}
