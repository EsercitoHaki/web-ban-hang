package com.example.webbanhang.services;

import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    @Override
    public User createUser(UserDTO userDTO) throws DataNotFoundException {
        String phoneNumber = userDTO.getPhoneNumber();
        //Kim tra xem có số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Số điện thoại đã tồn tại");
        }
        //Convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(String.valueOf(userDTO.getFacebookAccountId()))
                .googleAccountId(String.valueOf(userDTO.getGoogleAccountId()))
                .build();
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        newUser.setRole(role);
        //Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            //String encodePassword = passwordEnCoder.encode(password);
            //newUser.setPassword(encodePassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) {

        return null;
    }
}

