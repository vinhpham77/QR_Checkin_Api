package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vinhpham.qrcheckinapi.entities.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
