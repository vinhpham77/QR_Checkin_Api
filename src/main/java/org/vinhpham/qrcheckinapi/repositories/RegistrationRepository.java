package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vinhpham.qrcheckinapi.entities.Registration;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByUsernameAndEventId(String username, Long eventId);

    Optional<Registration> findByEventIdAndUsername(Long eventId, String username);

    @EntityGraph(attributePaths = {"event"})
    Page<Registration> findByUsername(String username, Pageable pageable);

    Page<Registration> findByEventIdAndAcceptedAtIsNull(Long eventId, Pageable pageable);
    Page<Registration> findByEventIdAndAcceptedAtIsNotNull(Long eventId, Pageable pageable);
}
