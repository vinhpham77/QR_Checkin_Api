package org.vinhpham.qrcheckinapi.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vinhpham.qrcheckinapi.entities.Device;
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

    @Transactional
    public Device save(Device device) {
        return deviceRepository.save(device);
    }

    @Transactional
    public void deleteById(String deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    @Transactional
    public void deleteMany(Set<Device> devices) {
        List<String> deviceIds = List.of(devices.stream().map(Device::getDeviceId).toArray(String[]::new));
        deviceRepository.deleteAllById(deviceIds);
    }
}
