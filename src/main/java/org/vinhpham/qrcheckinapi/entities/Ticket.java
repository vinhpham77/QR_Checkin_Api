package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.vinhpham.qrcheckinapi.utils.Utils;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @Size(max = 64)
    @NotNull
    @Column(name = "qr_code", nullable = false, length = 64)
    private String qrCode;

    @Column(name = "username")
    private String username;

    @NotNull
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "check_in_at")
    private Date checkInAt;

    @Column(name = "price")
    private Double price;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        qrCode = Utils.generateUUID();
    }
}
