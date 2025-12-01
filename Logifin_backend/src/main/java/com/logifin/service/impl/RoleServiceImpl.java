package com.logifin.service.impl;

import com.logifin.dto.RoleDTO;
import com.logifin.entity.Role;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new DuplicateResourceException("Role", "name", roleDTO.getRoleName());
        }

        Role role = mapToEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        return mapToDTO(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleByName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        return mapToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        if (!existingRole.getRoleName().equals(roleDTO.getRoleName())
                && roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new DuplicateResourceException("Role", "name", roleDTO.getRoleName());
        }

        existingRole.setRoleName(roleDTO.getRoleName());
        existingRole.setDescription(roleDTO.getDescription());

        Role updatedRole = roleRepository.save(existingRole);
        return mapToDTO(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role", "id", id);
        }
        roleRepository.deleteById(id);
    }

    private RoleDTO mapToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private Role mapToEntity(RoleDTO dto) {
        return Role.builder()
                .roleName(dto.getRoleName())
                .description(dto.getDescription())
                .build();
    }
}
