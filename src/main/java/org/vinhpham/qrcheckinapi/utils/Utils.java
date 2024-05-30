package org.vinhpham.qrcheckinapi.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.UUID;

public class Utils {
    public static Integer toInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean isUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static boolean isInEventRadius(double lat1, double lon1, double lat2, double lon2, double radius) {
        var distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radius;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

        double dLat  = Math.toRadians((lat2 - lat1));
        double dLong = Math.toRadians((lon2 - lon1));

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = haversin(dLat) + Math.cos(lat1) * Math.cos(lat2) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c * 1000; // <-- d in meters
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static double toDouble(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    public static Pageable getCreatedAtPageable(Integer page, int size) {
        return getPageable(page, size, "createdAt", false);
    }

    public static Pageable getPageable(Integer page, int size, String sortField, boolean isAsc) {
        if (page == null || page < 1) {
            page = 1;
        }

        page--;

        if (size < 0) {
            size = 10;
        }

        Sort sort = isAsc ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        return PageRequest.of(page, size, sort);
    }
}
