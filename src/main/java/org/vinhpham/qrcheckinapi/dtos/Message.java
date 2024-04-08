package org.vinhpham.qrcheckinapi.dtos;

public record Message(String message) {
    @Override
    public String toString() {
        return "{\"message\":\"" + message + "\"}";
    }
}
