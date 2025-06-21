package com.digit.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

/**
 * User Registration Data Transfer Object
 * 
 * @author System
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    
    /**
     * Username for login
     */
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username length must be between 3-20 characters")
    private String username;
    
    /**
     * Login password
     */
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 20, message = "Password length must be between 6-20 characters")
    private String password;
    
    /**
     * Email address
     */
    @Email(message = "Invalid email format")
    private String email;
    
    /**
     * Phone number
     */
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
    private String phone;
}
