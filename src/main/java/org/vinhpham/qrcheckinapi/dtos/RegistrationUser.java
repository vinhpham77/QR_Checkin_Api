package org.vinhpham.qrcheckinapi.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RegistrationUser {
    private Long registrationId;
    private String username;
    private String avatar;
    private String fullName;
    private Boolean sex;
    private String email;
    private Date createdAt;
    private Date acceptedAt;
}
