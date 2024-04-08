package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class UserDeviceId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1106598718409921950L;

    @NotNull
    @Size(max = 50)
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotNull
    @Size(max = 64)
    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserDeviceId entity = (UserDeviceId) o;
        return Objects.equals(this.deviceId, entity.deviceId) &&
                Objects.equals(this.username, entity.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, username);
    }

}
