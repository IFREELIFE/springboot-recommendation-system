package com.recommendation.homestay.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 6, max = 20, message = "Phone number length is invalid")
    @Pattern(regexp = "^\\+?[0-9\\- ]{6,20}$", message = "Phone number format is invalid")
    private String phone;

    private String avatar;

    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;
}
