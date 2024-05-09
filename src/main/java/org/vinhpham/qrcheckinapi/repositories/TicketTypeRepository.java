package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vinhpham.qrcheckinapi.entities.TicketType;

import java.util.List;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findAllByEventId(Long eventId);
}
