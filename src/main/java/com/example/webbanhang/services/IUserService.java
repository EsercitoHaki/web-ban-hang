package com.example.webbanhang.services;

import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.User;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
}
