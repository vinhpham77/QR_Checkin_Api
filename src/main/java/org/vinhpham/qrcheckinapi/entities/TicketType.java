package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @PositiveOrZero
    @Column(name = "quantity")
    private Integer quantity;

    @NotNull
    @Column(name = "event_id")
    private Long eventId;

    @NotNull
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @NotNull
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @NotNull
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "price")
    private Double price;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        deletedAt = null;
        deletedBy = null;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
