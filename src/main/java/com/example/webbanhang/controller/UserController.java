package com.example.webbanhang.controller;


import com.example.webbanhang.components.GoogleTokenUtils;
import com.example.webbanhang.dtos.*;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.responses.LoginResponse;
import com.example.webbanhang.responses.RegisterResponse;
import com.example.webbanhang.responses.UserResponse;
import com.example.webbanhang.services.IUserService;
import com.example.webbanhang.services.UserService;
import com.example.webbanhang.components.LocalizationUtils;
import com.example.webbanhang.utils.MessageKeys;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    private final RoleRepository roleRepository;
    private final GoogleTokenUtils googleTokenUtils;
    private final UserRepository userRepository;

    @PostMapping("/register")
    //can we register an "admin" user ?
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        RegisterResponse registerResponse = new RegisterResponse();

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();

            registerResponse.setMessage(errorMessages.toString());
            return ResponseEntity.badRequest().body(registerResponse);
        }

        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
            return ResponseEntity.badRequest().body(registerResponse);
        }

        try {
            User user = userService.createUser(userDTO);
            registerResponse.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY));
            registerResponse.setUser(user);
            return ResponseEntity.ok(registerResponse);
        } catch (Exception e) {
            registerResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(registerResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) {
        // Kiểm tra thông tin đăng nhập và sinh token
        try {
            String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId()
            );
            // Trả về token trong response
            return ResponseEntity.ok(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(token)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                            .build()
            );
        }
    }
    @PostMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/details/{userId}")
    public ResponseEntity<UserResponse> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailsFromToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (user.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok(UserResponse.fromUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<String> handleGoogleLogin(@Valid @RequestBody UserDTO userDTO) {
        try {
            // Kiểm tra nếu tài khoản Google đã tồn tại
            boolean exists = userService.existsByGoogleAccountId(userDTO.getGoogleAccountId());
            if (exists) {
                return ResponseEntity.ok("Google account already exists");
            }

            // Lưu tài khoản Google mới
            User user = userService.createUser(userDTO);

            return ResponseEntity.status(201).body("Google account registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
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
        String googleAccountId = payload.getSubject(); // Google Account ID

        // Kiểm tra nếu tài khoản Google đã tồn tại
        Optional<User> optionalUser = userRepository.findByGoogleAccountId(Integer.parseInt(googleAccountId));
        if (optionalUser.isPresent()) {
            return optionalUser.get(); // Trả về user nếu đã tồn tại
        }

        // Nếu chưa tồn tại, tạo user mới và lưu vào DB
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User newUser = User.builder()
                .fullName(name)
                .googleAccountId(Integer.parseInt(googleAccountId)) // Chuyển đổi sang int
                .role(defaultRole) // Quyền mặc định, ví dụ: USER
                .build();

        return userRepository.save(newUser);
    }
}
