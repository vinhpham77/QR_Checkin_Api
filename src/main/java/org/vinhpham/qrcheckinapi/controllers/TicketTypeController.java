package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.services.TicketTypeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket-types")
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    @GetMapping("{eventId}")
    public ResponseEntity<?> getByEventId(@PathVariable Long eventId) {
        var items = ticketTypeService.getByEventId(eventId);

        return Success.ok(items);
    }
}
