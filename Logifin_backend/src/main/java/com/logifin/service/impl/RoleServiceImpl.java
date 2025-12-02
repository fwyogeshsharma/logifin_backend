package com.logifin.service.impl;

import com.logifin.config.CacheConfig;
import com.logifin.dto.PagedResponse;
import com.logifin.dto.RoleDTO;
import com.logifin.entity.Role;
import com.logifin.exception.DuplicateResourceException;
import com.logifin.exception.ResourceNotFoundException;
import com.logifin.repository.RoleRepository;
import com.logifin.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @CacheEvict(value = CacheConfig.CACHE_ROLES, allEntries = true)
    public RoleDTO createRole(RoleDTO roleDTO) {
        log.debug("Creating role: {}", roleDTO.getRoleName());
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new DuplicateResourceException("Role", "name", roleDTO.getRoleName());
        }

        Role role = mapToEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        return mapToDTO(savedRole);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ROLE_BY_ID, key = "#id", unless = "#result == null")
    public RoleDTO getRoleById(Long id) {
        log.debug("Fetching role by id: {} from database", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
        return mapToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ROLE_BY_NAME, key = "#roleName", unless = "#result == null")
    public RoleDTO getRoleByName(String roleName) {
        log.debug("Fetching role by name: {} from database", roleName);
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));
        return mapToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_ROLES, unless = "#result == null || #result.isEmpty()")
    public List<RoleDTO> getAllRoles() {
        log.debug("Fetching all roles from database");
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_ROLE_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_ROLE_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_ROLES, allEntries = true)
    })
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        log.debug("Updating role with id: {}", id);
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
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_ROLE_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_ROLE_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_ROLES, allEntries = true)
    })
    public void deleteRole(Long id) {
        log.debug("Deleting role with id: {}", id);
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

    // Paginated methods

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> getAllRoles(Pageable pageable) {
        log.debug("Fetching all roles with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Role> rolePage = roleRepository.findAll(pageable);
        List<RoleDTO> roleDTOs = rolePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(rolePage, roleDTOs);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RoleDTO> searchRoles(String keyword, Pageable pageable) {
        log.debug("Searching roles by keyword: {} with pagination: page={}, size={}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        Page<Role> rolePage = roleRepository.searchByKeyword(keyword, pageable);
        List<RoleDTO> roleDTOs = rolePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return PagedResponse.of(rolePage, roleDTOs);
    }
}
