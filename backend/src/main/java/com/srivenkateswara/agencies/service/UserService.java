package com.srivenkateswara.agencies.service;

import com.srivenkateswara.agencies.dto.UserDto;
import com.srivenkateswara.agencies.dto.UserRoleUpdateRequest;
import com.srivenkateswara.agencies.entity.ERole;
import com.srivenkateswara.agencies.entity.Role;
import com.srivenkateswara.agencies.entity.User;
import com.srivenkateswara.agencies.exception.BadRequestException;
import com.srivenkateswara.agencies.exception.ResourceNotFoundException;
import com.srivenkateswara.agencies.repository.RoleRepository;
import com.srivenkateswara.agencies.repository.UserRepository;
import com.srivenkateswara.agencies.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return authService.mapToUserDto(user);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            ERole eRole;
            try {
                eRole = ERole.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role name: " + roleName);
            }

            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
            roles.add(role);
        }

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return authService.mapToUserDto(updatedUser);
    }

    public User getCurrentlyAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadRequestException("User is not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }
}
