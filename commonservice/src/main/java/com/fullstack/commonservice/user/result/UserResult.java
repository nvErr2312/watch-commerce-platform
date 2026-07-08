package com.fullstack.commonservice.user.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResult {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String role;
    private String status;
}
