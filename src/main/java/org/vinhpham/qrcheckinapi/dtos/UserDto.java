package org.vinhpham.qrcheckinapi.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "{username.notBlank}")
    @Length(min = 6, max = 50, message = "Tên đăng nhập phải có ít nhất 6 ký tự và tối đa 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Length(min = 8, max = 50, message = "Mật khẩu phải có ít nhất 8 ký tự và tối đa 50 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Length(max = 50, message = "Email không được quá 50 ký tự")
    private String email;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "CMND/CCCD phải có từ 9 đến 12 chữ số")
    private String idNo;

}
