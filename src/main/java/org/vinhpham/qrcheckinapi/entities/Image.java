package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Size(max = 10)
    @NotNull
    @ColumnDefault("")
    @Column(name = "extension", nullable = false, length = 10)
    private String extension;

    @NotNull
    @Column(name = "status", nullable = false)
    private Boolean status = false;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = new Date();
    }
}
