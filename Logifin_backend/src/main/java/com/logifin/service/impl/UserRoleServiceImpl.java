package com.logifin.service.impl;

import com.logifin.dto.SetRoleRequest;
import com.logifin.entity.Role;
import com.logifin.entity.User;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.repository.UserRepository;
import com.logifin.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void setUserRole(SetRoleRequest request) {
        log.info("Setting role {} for user {}", request.getRoleId(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        user.setRole(role);
        userRepository.save(user);

        log.info("Successfully set role '{}' for user '{}'", role.getRoleName(), user.getEmail());
    }
}
