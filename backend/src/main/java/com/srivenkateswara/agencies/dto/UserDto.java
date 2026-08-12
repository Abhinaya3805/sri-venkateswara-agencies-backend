package com.srivenkateswara.agencies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Boolean enabled;
    private Set<String> roles;
}
