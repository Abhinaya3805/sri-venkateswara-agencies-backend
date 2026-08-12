package com.srivenkateswara.agencies.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or Mobile Number is required")
    private String emailOrMobile;

    @NotBlank(message = "Password is required")
    private String password;
}
