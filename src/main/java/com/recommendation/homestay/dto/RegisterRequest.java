package com.recommendation.homestay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "用户名为必填项")
    @Size(min = 3, max = 50, message = "用户名长度需在3到50字符之间")
    private String username;
    
    @NotBlank(message = "密码为必填项")
    @Size(min = 6, max = 100, message = "密码长度至少6位")
    private String password;
    
    @NotBlank(message = "邮箱为必填项")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    private String role; // USER、LANDLORD、ADMIN
}
