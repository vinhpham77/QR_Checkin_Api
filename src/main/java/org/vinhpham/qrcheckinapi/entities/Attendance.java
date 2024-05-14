package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendances")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "username")
    private String username;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "check_in_at")
    private Date checkInAt;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "check_out_at")
    private Date checkOutAt;

    @Size(max = 255)
    @Column(name = "check_in_img")
    private String checkInImg;

    @Size(max = 255)
    @Column(name = "check_out_img")
    private String checkOutImg;

    @Size(max = 255)
    @Column(name = "qr_check_in_img")
    private String qrCheckInImg;

    @Size(max = 255)
    @Column(name = "qr_check_out_img")
    private String qrCheckOutImg;
}
