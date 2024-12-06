package com.example.webbanhang.services;

import com.example.webbanhang.components.GoogleTokenUtils;
import com.example.webbanhang.components.JwtTokenUtils;
import com.example.webbanhang.dtos.UserDTO;
import com.example.webbanhang.exceptions.DataNotFoundException;
import com.example.webbanhang.exceptions.PremissionDenyException;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtil;
    private final GoogleTokenUtils googleTokenUtils;


    private final AuthenticationManager authenticationManager;



    @Override
    public boolean existsByGoogleAccountId(int googleAccountId) {
        return userRepository.existsByGoogleAccountId(googleAccountId);
    }


    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();

        // Kiểm tra nếu tài khoản Google đã tồn tại
        if (userDTO.getGoogleAccountId() != 0) {
            if (userRepository.existsByGoogleAccountId(userDTO.getGoogleAccountId())) {
                throw new DataIntegrityViolationException("Google account ID already exists");
            }
        }

        //Kim tra xem có số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Số điện thoại đã tồn tại");
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
        // Mã hóa mật khẩu nếu không có tài khoản Google/Facebook
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String password = userDTO.getPassword();
            if (password == null || password.isEmpty()) {
                throw new DataIntegrityViolationException("Password is required for standard accounts");
            }
            String encodePassword = passwordEncoder.encode(password);
            newUser.setPassword(encodePassword);
        }

        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
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
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password,
                existingUser.getAuthorities()
        );

        //authenticate with Java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTokenUtil.generateToken(existingUser);
    }

    public User processGoogleLogin(String googleToken) throws Exception {
        // Decode Google Token
        GoogleIdToken idToken = googleTokenUtils.verifyToken(googleToken);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google Token");
        }

        // Lấy thông tin từ Google Token
        GoogleIdToken.Payload payload = idToken.getPayload();
//        String email = payload.getEmail();
        String name = (String) payload.get("name");
        int googleAccountId = Integer.parseInt(payload.getSubject()); // Google Account ID

        // Kiểm tra nếu tài khoản Google đã tồn tại
        Optional<User> optionalUser = userRepository.findByGoogleAccountId(googleAccountId);
        if (optionalUser.isPresent()) {
            return optionalUser.get(); // Trả về user nếu đã tồn tại
        }

        // Nếu chưa tồn tại, tạo user mới và lưu vào DB
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = User.builder()
                .fullName(name)
                .googleAccountId(googleAccountId)
                .role(defaultRole) // Quyền mặc định
                .build();

        return userRepository.save(newUser);
    }
}

