package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.Device;
import org.vinhpham.qrcheckinapi.entities.User;
import org.vinhpham.qrcheckinapi.repositories.DeviceRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public Optional<Device> findById(String deviceId) {
        return deviceRepository.findById(deviceId);
    }

    public Optional<Device> findByUser(User user) {
        return deviceRepository.findByUser(user);
    }

    @Transactional
    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    @Transactional
    public void deleteById(String deviceId) {
        deviceRepository.deleteById(deviceId);
    }

}
