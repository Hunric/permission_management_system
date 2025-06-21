// 所属包: com.digit.user.vo
package com.digit.user.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * User Registration Response Value Object
 * 
 * @author System
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterVO {

    /**
     * User unique identifier
     */
    private Long userId;

    /**
     * Username
     */
    private String username;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * Phone number
     */
    private String phone;
}