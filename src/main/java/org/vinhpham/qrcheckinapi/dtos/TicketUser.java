package org.vinhpham.qrcheckinapi.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TicketUser {
    private Long ticketId;
    private String username;
    private String fullName;
    private Boolean sex;
    private String email;
    private String avatar;
    private String ticketType;
    private Date createdAt;
    private Date checkInAt;
}
