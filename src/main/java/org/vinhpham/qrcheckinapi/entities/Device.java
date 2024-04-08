package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "devices")
public class Device {

    @Id
    @Size(max = 64)
    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Size(max = 255)
    @NotNull
    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @NotNull
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = new Date();
        if (deviceId == null || deviceId.isBlank()) deviceId = generateDeviceId();
        if (deviceName == null || deviceName.isBlank()) deviceName = "Anonymous";
        if (updatedAt == null) updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    private static String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
}
