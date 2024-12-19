package com.example.webbanhang.services;

import com.example.webbanhang.dtos.UpdateUserDTO;
import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.exceptions.InvalidParamException;
import com.example.webbanhang.exceptions.InvalidPasswordException;
import com.example.webbanhang.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
    Page<User> findAll(String keyword, Pageable pageable) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;
    void resetPassword(Long userId, String newPassword)
            throws InvalidPasswordException, DataNotFoundException ;
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;

    Long countUsersWithRoleId(Long roleId);
}
