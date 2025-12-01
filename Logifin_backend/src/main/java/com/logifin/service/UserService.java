package com.logifin.service;

import com.logifin.dto.UserDTO;

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
}
