package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.dtos.HandleException;
import org.vinhpham.qrcheckinapi.dtos.ItemCounter;
import org.vinhpham.qrcheckinapi.dtos.TicketDetail;
import org.vinhpham.qrcheckinapi.entities.Ticket;
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

    @Transactional
    public void checkIn(String code, Long eventId) {
        var ticket = ticketRepository.findByQrCode(code);

        var event = eventService.get(eventId);

        if (event == null) {
            throw new HandleException("error.event.not.found", HttpStatus.NOT_FOUND);
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!event.getCreatedBy().equals(username)) {
            throw new HandleException("error.event.user.not.permitted", HttpStatus.FORBIDDEN);
        }

        var ticketType = ticket.getTicketType();

        if (ticketType == null) {
            throw new HandleException("error.ticket.type.not.found", HttpStatus.NOT_FOUND);
        }

        if (!ticketType.getEventId().equals(eventId)) {
            throw new HandleException("error.ticket.event.not.match", HttpStatus.BAD_REQUEST, event.getName());
        }

        var now = new Date();

        if (event.getStartAt().after(now)) {
            throw new HandleException("error.event.not.started", HttpStatus.BAD_REQUEST);
        }

        if (event.getEndAt().before(now)) {
            throw new HandleException("error.event.ended", HttpStatus.BAD_REQUEST);
        }

        if (ticket.getCheckInAt() != null) {
            throw new HandleException("error.ticket.already.checked.in", HttpStatus.BAD_REQUEST);
        }

        ticket.setCheckInAt(now);

        ticketRepository.save(ticket);
    }

    public ItemCounter<TicketDetail> getTickets(Integer page, Integer size) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (page == null || page < 1) {
            page = 1;
        }

        page--;

        if (size < 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        var tickets = ticketRepository.findByUsername(username, pageable);
        var total = tickets.getTotalElements();
        var ticketDetailPage = tickets.map(ticket -> {
            var ticketType = ticket.getTicketType();
            var event = eventService.get(ticketType.getEventId());

            return TicketDetail.builder()
                    .id(ticket.getId())
                    .ticketTypeName(ticketType.getName())
                    .price(ticketType.getPrice())
                    .eventName(event.getName())
                    .checkInAt(ticket.getCheckInAt())
                    .createdAt(ticket.getCreatedAt())
                    .location(event.getLocation())
                    .qrCode(ticket.getQrCode())
                    .eventId(event.getId())
                    .username(ticket.getUsername())
                    .build();
        });

        var ticketDetails = ticketDetailPage.stream().toList();
        return new ItemCounter<>(ticketDetails, total);
    }
}
