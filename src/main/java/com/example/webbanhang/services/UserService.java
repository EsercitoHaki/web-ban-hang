package com.example.webbanhang.services;

import com.example.webbanhang.components.JwtTokenUtils;
import com.example.webbanhang.components.LocalizationUtils;
import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.exceptions.PremissionDenyException;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;
    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        //Kim tra xem có số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));
        if (role.getName().toUpperCase().equals(Role.ADMIN))
        {
            throw new PremissionDenyException("You cannot register an admin account");
        }
        //Convert from userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();
        newUser.setRole(role);
        //Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            String encodePassword = passwordEncoder.encode(password);
            newUser.setPassword(encodePassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password, Long roleId) throws Exception {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isEmpty())
        {
            throw new DataNotFoundException("Invalid phone number / password");
        }
        //return optionalUser.get();
        User existingUser = optionalUser.get();

        if (existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0)
        {
            if (!passwordEncoder.matches(password, existingUser.getPassword()))
            {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }

        Optional<Role> optionalRole = roleRepository.findById(roleId);

        if (optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId()))
        {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }

        if(!optionalUser.get().isActive())
        {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );

        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }
}

