package com.srivenkateswara.agencies.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequest {

    @NotEmpty(message = "Roles set cannot be empty")
    private Set<String> roles; // e.g., ["ROLE_USER", "ROLE_ADMIN"]
}
