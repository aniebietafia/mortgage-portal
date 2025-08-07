package com.portal.mortgage.auth.dto;

import com.portal.mortgage.datamodel.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Role role;
    private String token;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
