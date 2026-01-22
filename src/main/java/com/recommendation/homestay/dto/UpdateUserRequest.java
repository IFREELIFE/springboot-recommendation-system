package com.recommendation.homestay.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateUserRequest {
    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^(\\+?[0-9\\s\\-]{6,20})?$", message = "手机号格式不正确（可留空）")
    private String phone;

    private String avatar;

    @Size(min = 6, max = 100, message = "密码长度至少6位")
    private String password;
}
