package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vinhpham.qrcheckinapi.entities.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
