package org.vinhpham.qrcheckinapi.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.vinhpham.qrcheckinapi.dtos.Failure;
import org.vinhpham.qrcheckinapi.dtos.Message;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");

        ResponseEntity<Message> responseEntity = Failure.response("Có lỗi xảy ra. Vui lòng thử lại sau", HttpStatus.UNAUTHORIZED);
        String message = Objects.requireNonNull(responseEntity.getBody()).toString();
        response.getWriter().write(message);
    }
}
