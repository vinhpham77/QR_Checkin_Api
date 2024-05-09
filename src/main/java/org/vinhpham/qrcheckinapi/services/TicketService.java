package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.entities.Ticket;
import org.vinhpham.qrcheckinapi.repositories.EventRepository;
import org.vinhpham.qrcheckinapi.repositories.TicketRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketTypeService ticketTypeService;
    private final EventService eventService;

    @Transactional
    public void purchaseTicket(Long ticketTypeId) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        var ticketType = ticketTypeService.get(ticketTypeId);

        if (ticketType == null) {
            throw new HandleException("error.ticket.type.not.found", HttpStatus.NOT_FOUND);
        }

        var event = eventService.get(ticketType.getEventId());
        var now = new Date();

        if (event.getEndAt().before(now)) {
            throw new HandleException("error.event.ended", HttpStatus.BAD_REQUEST);
        }

        if (ticketType.getQuantity() <= 0) {
            throw new HandleException("error.ticket.type.sold.out", HttpStatus.BAD_REQUEST, ticketType.getName());
        }

        ticketType.setQuantity(ticketType.getQuantity() - 1);
        ticketTypeService.save(ticketType);

        var ticket = Ticket.builder()
                .ticketType(ticketType)
                .price(ticketType.getPrice())
                .username(username)
                .build();

        ticketRepository.save(ticket);
    }
}
