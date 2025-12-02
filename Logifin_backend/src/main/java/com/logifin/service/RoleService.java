package com.logifin.service;

import com.logifin.dto.PagedResponse;
import com.logifin.dto.RoleDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleDTO createRole(RoleDTO roleDTO);

    RoleDTO getRoleById(Long id);

    RoleDTO getRoleByName(String roleName);

    List<RoleDTO> getAllRoles();

    RoleDTO updateRole(Long id, RoleDTO roleDTO);

    void deleteRole(Long id);

    // Paginated methods
    PagedResponse<RoleDTO> getAllRoles(Pageable pageable);

    PagedResponse<RoleDTO> searchRoles(String keyword, Pageable pageable);
}
