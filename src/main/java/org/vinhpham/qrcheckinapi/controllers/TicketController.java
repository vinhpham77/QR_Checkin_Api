package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vinhpham.qrcheckinapi.dtos.PurchaseRequest;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.dtos.TicketCheckinRequest;
import org.vinhpham.qrcheckinapi.services.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest purchaseRequest) {
        ticketService.purchaseTicket(purchaseRequest.getTicketTypeId());
        return Success.ok(null);
    }

    @PutMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody TicketCheckinRequest request) {
        ticketService.checkIn(request.getCode(), request.getEventId());
        return Success.ok(null);
    }

    @GetMapping
    public ResponseEntity<?> getTickets(@RequestParam(required = false, name = "page") Integer page,
                                        @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getTickets(page, size));
    }

    @GetMapping("/{id}/buyers")
    public ResponseEntity<?> getBuyers(@PathVariable Long id, @RequestParam(required = false, name = "page") Integer page,
                                       @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getBuyers(id, page, size, false));
    }

    @GetMapping("/{id}/check-ins")
    public ResponseEntity<?> getCheckIns(@PathVariable Long id, @RequestParam(required = false, name = "page") Integer page,
                                         @RequestParam(required = false, name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ticketService.getBuyers(id, page, size, true));
    }
}
