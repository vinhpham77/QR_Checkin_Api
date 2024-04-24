package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
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
public class EventCategoryId implements Serializable {
    @Serial
    private static final long serialVersionUID = -4526726122448322202L;

    @NotNull
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "category_id")
    private Long categoryId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        EventCategoryId entity = (EventCategoryId) o;
        return Objects.equals(this.eventId, entity.eventId) &&
                Objects.equals(this.categoryId, entity.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, categoryId);
    }

}
