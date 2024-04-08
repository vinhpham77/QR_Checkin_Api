package org.vinhpham.qrcheckinapi.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.vinhpham.qrcheckinapi.dtos.Role;
import org.vinhpham.qrcheckinapi.dtos.Status;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Basic
    @Column(name = "hash_password", nullable = false, length = 60)
    private String hashPassword;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Basic
    @Column(name = "full_name", length = 120)
    private String fullName;

    @Basic
    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Basic
    @Column(name = "sex")
    private Boolean sex;

    @Basic
    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Basic
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = Status.ACTIVE;
        if (role == null) role = Role.ROLE_user;
        if (createdAt == null) createdAt = new Date();
        if (updatedAt == null) updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.hashPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !status.equals(Status.BANNED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status.equals(Status.ACTIVE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(hashPassword, that.hashPassword)) return false;
        if (!Objects.equals(role, that.role)) return false;
        if (!Objects.equals(status, that.status)) return false;
        if (!Objects.equals(fullName, that.fullName)) return false;
        if (!Objects.equals(email, that.email)) return false;
        if (!Objects.equals(sex, that.sex)) return false;
        if (!Objects.equals(birthdate, that.birthdate)) return false;
        if (!Objects.equals(createdAt, that.createdAt)) return false;

        return Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, hashPassword, role, status,
                fullName, email, sex, birthdate, createdAt, updatedAt);
    }

}
