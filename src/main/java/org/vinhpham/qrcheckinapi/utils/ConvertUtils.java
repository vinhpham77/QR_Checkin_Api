package org.vinhpham.qrcheckinapi.utils;

import java.util.UUID;

public class ConvertUtils {
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
}
