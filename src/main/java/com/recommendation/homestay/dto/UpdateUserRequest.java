package com.recommendation.homestay.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 20, message = "Phone number is too long")
    private String phone;

    private String avatar;

    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;
}
