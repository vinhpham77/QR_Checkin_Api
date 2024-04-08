package org.vinhpham.qrcheckinapi.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Login {

    @NotBlank(message = "Tài khoản không được để trống")
    String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    String password;

}
