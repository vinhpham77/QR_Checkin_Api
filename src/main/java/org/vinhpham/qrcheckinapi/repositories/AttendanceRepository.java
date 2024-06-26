package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.vinhpham.qrcheckinapi.entities.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Attendance findByRegistrationId(Long registrationId);

    Attendance findByUsernameAndEventId(String username, Long id);

    Page<Attendance> findByEventId(Long eventId, Pageable pageable);
}
