package org.vinhpham.qrcheckinapi.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vinhpham.qrcheckinapi.common.Constants;
import org.vinhpham.qrcheckinapi.dtos.Success;
import org.vinhpham.qrcheckinapi.services.AttendanceService;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkIn(
            @PathVariable Long id,
            @RequestParam("qrImage") MultipartFile qrImage,
            @RequestParam(value = "portraitImage", required = false) MultipartFile portraitImage,
            @RequestParam("code") String code,
            HttpServletRequest request) {

        String latitude = request.getHeader(Constants.KEY_LATITUDE);
        String longitude = request.getHeader(Constants.KEY_LONGITUDE);

        attendanceService.checkIn(id, qrImage, portraitImage, code, latitude, longitude);
        return Success.ok(null);
    }
}
