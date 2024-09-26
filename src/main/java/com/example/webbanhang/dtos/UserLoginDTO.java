package com.example.webbanhang.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDTO {
    @NotBlank(message = "Số điện thoại là bắt buộc")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}

