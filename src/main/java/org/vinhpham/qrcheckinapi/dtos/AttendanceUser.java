package org.vinhpham.qrcheckinapi.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AttendanceUser {
    private Long attendanceId;
    private String username;
    private String fullName;
    private Boolean sex;
    private String email;
    private String avatar;
    private Boolean isCheckOutRequired;
    private Boolean isCaptureRequired;
    private Date checkInAt;
    private Date checkOutAt;
    private String checkInImg;
    private String checkOutImg;
    private String qrCheckInImg;
    private String qrCheckOutImg;
}
