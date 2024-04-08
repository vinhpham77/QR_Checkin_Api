package org.vinhpham.qrcheckinapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vinhpham.qrcheckinapi.entities.UserDevice;
import org.vinhpham.qrcheckinapi.entities.UserDeviceId;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UserDeviceId> {
}
