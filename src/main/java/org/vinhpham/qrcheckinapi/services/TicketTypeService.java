package org.vinhpham.qrcheckinapi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.TicketType;
import org.vinhpham.qrcheckinapi.repositories.TicketTypeRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    private final TicketTypeRepository ticketTypeRepository;

    public List<TicketType> getByEventId(Long eventId) {
        return ticketTypeRepository.findAllByEventId(eventId);
    }

    public TicketType get(Long id) {
        return ticketTypeRepository.findById(id).orElse(null);
    }

    public TicketType save(TicketType ticketType) {
        return ticketTypeRepository.save(ticketType);
    }

    public List<TicketType> saveAll(List<TicketType> ticketTypes) {
        return ticketTypeRepository.saveAll(ticketTypes);
    }

    public void delete(TicketType ticketType) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();

        ticketType.setDeletedBy(username);
        ticketType.setDeletedAt(new Date());
        ticketTypeRepository.save(ticketType);
    }
}
