package com.fullstack.commonservice.user.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResult {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String role;
    private String status;
}
