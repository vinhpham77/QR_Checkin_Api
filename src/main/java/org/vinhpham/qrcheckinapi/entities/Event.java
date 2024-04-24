package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.vinhpham.qrcheckinapi.utils.ConvertUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "slots", columnDefinition = "int UNSIGNED")
    private Long slots;

    @Basic
    @Column(name = "start_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startAt;

    @Basic
    @Column(name = "end_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endAt;

    @Size(max = 255)
    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @NotNull
    @Column(name = "radius", nullable = false)
    private Double radius;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    @NotNull
    @Column(name = "is_approved", nullable = false)
    private Boolean isApproved;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Basic
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Basic
    @NotNull
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private User deletedBy;

    @Size(max = 64)
    @NotNull
    @Column(name = "checkin_qr_code", nullable = false, length = 64, unique = true)
    private String checkinQrCode;

    @Size(max = 64)
    @Column(name = "checkout_qr_code", length = 64, unique = true)
    private String checkoutQrCode;

    @ManyToMany
    @JoinTable(
            name = "event_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @Size(max = 255)
    @Column(name = "background_url")
    private String backgroundUrl;

    @PrePersist
    protected void onCreate() {
        if (isRequired == null) isRequired = false;
        if (isApproved == null) isApproved = false;
        if (createdAt == null) createdAt = new Date();
        if (updatedAt == null) updatedAt = new Date();
        checkinQrCode = ConvertUtils.generateUUID();
        setCheckoutQrCode();
    }

    public void setCheckoutQrCode() {
        if (checkoutQrCode == null || checkoutQrCode.isBlank()) {
            this.checkoutQrCode = ConvertUtils.generateUUID();
        } else if (!ConvertUtils.isUUID(checkoutQrCode)) {
            this.checkoutQrCode = null;
        }
    }
}