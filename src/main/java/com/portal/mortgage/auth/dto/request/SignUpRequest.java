package com.portal.mortgage.auth.dto.request;

import com.portal.mortgage.datamodel.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Role role;
}