package org.vinhpham.qrcheckinapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vinhpham.qrcheckinapi.dtos.PurchaseRequest;
import org.vinhpham.qrcheckinapi.dtos.Success;
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
}
