package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.ApiResponse;
import com.srivenkateswara.agencies.dto.UserDto;
import com.srivenkateswara.agencies.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile endpoints")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Get logged-in user profile details")
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        UserDto user = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile fetched successfully", user));
    }
}
