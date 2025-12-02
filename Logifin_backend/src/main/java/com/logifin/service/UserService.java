package com.logifin.service;

import com.logifin.dto.PagedResponse;
import com.logifin.dto.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    UserDTO getUserByEmail(String email);

    List<UserDTO> getAllUsers();

    List<UserDTO> getActiveUsers();

    List<UserDTO> searchUsersByName(String name);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    void deactivateUser(Long id);

    void activateUser(Long id);

    // Paginated methods
    PagedResponse<UserDTO> getAllUsers(Pageable pageable);

    PagedResponse<UserDTO> getActiveUsers(Pageable pageable);

    PagedResponse<UserDTO> getInactiveUsers(Pageable pageable);

    PagedResponse<UserDTO> searchUsers(String keyword, Pageable pageable);

    PagedResponse<UserDTO> getUsersByRole(String roleName, Pageable pageable);
}
