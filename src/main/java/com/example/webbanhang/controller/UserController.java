package com.example.webbanhang.controller;


import com.example.webbanhang.components.GoogleTokenUtils;
import com.example.webbanhang.dtos.*;
import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.Token;
import com.example.webbanhang.models.User;
import com.example.webbanhang.repositories.RoleRepository;
import com.example.webbanhang.repositories.UserRepository;
import com.example.webbanhang.responses.LoginResponse;
import com.example.webbanhang.responses.RegisterResponse;
import com.example.webbanhang.responses.ResponseObject;
import com.example.webbanhang.responses.UserResponse;
import com.example.webbanhang.services.*;
import com.example.webbanhang.components.LocalizationUtils;
import com.example.webbanhang.utils.MessageKeys;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final IAuthService authService;
    private final ITokenService tokenService;

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
    public ResponseEntity<ResponseObject> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .message("Get user's detail successfully")
                    .data(UserResponse.fromUser(user))
                    .status(HttpStatus.OK)
                    .build()
            );
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


    //Angular, bấm đăng nhập gg, redirect đến trang đăng nhập google, đăng nhập xong có "code"
    //Từ "code" => google token => lấy ra các thông tin khác
    @GetMapping("/auth/social-login")
    public ResponseEntity<String> socialAuth(
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ){
        //request.getRequestURI()
        loginType = loginType.trim().toLowerCase();  // Loại bỏ dấu cách và chuyển thành chữ thường
        String url = authService.generateAuthUrl(loginType);
        return ResponseEntity.ok(url);
    }

    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        // Ví dụ đơn giản:
        return userAgent.toLowerCase().contains("mobile");
    }


    private ResponseEntity<ResponseObject> loginSocial(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request
    ) throws Exception {
        // Gọi hàm loginSocial từ UserService cho đăng nhập mạng xã hội
        String token = userService.loginSocial(userLoginDTO);

        // Xử lý token và thông tin người dùng
        String userAgent = request.getHeader("User-Agent");
        User userDetail = userService.getUserDetailsFromToken(token);
        Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

        // Tạo đối tượng LoginResponse
        LoginResponse loginResponse = LoginResponse.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                .token(jwtToken.getToken())
                .tokenType(jwtToken.getTokenType())
                .refreshToken(jwtToken.getRefreshToken())
                .username(userDetail.getUsername())
                .roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .id(userDetail.getId())
                .build();

        // Trả về phản hồi
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Login successfully")
                        .data(loginResponse)
                        .status(HttpStatus.OK)
                        .build()
        );
    }


    @GetMapping("/auth/social/callback")
    public ResponseEntity<ResponseObject> callback(
            @RequestParam("code") String code,
            @RequestParam("login_type") String loginType,
            HttpServletRequest request
    ) throws Exception {
        // Call the AuthService to get user info
        Map<String, Object> userInfo = authService.authenticateAndFetchProfile(code, loginType);

        if (userInfo == null) {
            return ResponseEntity.badRequest().body(new ResponseObject(
                    "Failed to authenticate", HttpStatus.BAD_REQUEST, null
            ));
        }

        // Extract user information from userInfo map
        String accountId = "";
        String name = "";
        String picture = "";
        String email = "";

        if (loginType.trim().equals("google")) {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            picture = (String) Objects.requireNonNullElse(userInfo.get("picture"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
        } else if (loginType.trim().equals("facebook")) {
            accountId = (String) Objects.requireNonNullElse(userInfo.get("id"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
            // Lấy URL ảnh từ cấu trúc dữ liệu của Facebook
            Object pictureObj = userInfo.get("picture");
            if (pictureObj instanceof Map) {
                Map<?, ?> pictureData = (Map<?, ?>) pictureObj;
                Object dataObj = pictureData.get("data");
                if (dataObj instanceof Map) {
                    Map<?, ?> dataMap = (Map<?, ?>) dataObj;
                    Object urlObj = dataMap.get("url");
                    if (urlObj instanceof String) {
                        picture = (String) urlObj;
                    }
                }
            }
        }

        // Tạo đối tượng UserLoginDTO
        UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                .email(email)
                .fullname(name)
                .password("")
                .phoneNumber("")
                .profileImage(picture)
                .build();

        if (loginType.trim().equals("google")) {
            userLoginDTO.setGoogleAccountId(accountId);
            //userLoginDTO.setFacebookAccountId("");
        } else if (loginType.trim().equals("facebook")) {
            userLoginDTO.setFacebookAccountId(accountId);
            //userLoginDTO.setGoogleAccountId("");
        }

        return this.loginSocial(userLoginDTO, request);
    }



























//    @PostMapping("/google-login")
//    public ResponseEntity<String> handleGoogleLogin(@Valid @RequestBody UserDTO userDTO) {
//        try {
//            // Kiểm tra nếu tài khoản Google đã tồn tại
//            boolean exists = userService.existsByGoogleAccountId(userDTO.getGoogleAccountId());
//            if (exists) {
//                return ResponseEntity.ok("Google account already exists");
//            }
//
//            // Lưu tài khoản Google mới
//            User user = userService.createUser(userDTO);
//
//            return ResponseEntity.status(201).body("Google account registered successfully");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//        }
//    }
//
//    public User processGoogleLogin(String googleToken) throws Exception {
//        // Decode Google Token
//        GoogleIdToken idToken = googleTokenUtils.verifyToken(googleToken);
//        if (idToken == null) {
//            throw new IllegalArgumentException("Invalid Google Token");
//        }
//
//        // Lấy thông tin từ Google Token
//        GoogleIdToken.Payload payload = idToken.getPayload();
////        String email = payload.getEmail();
//        String name = (String) payload.get("name");
//        String googleAccountId = payload.getSubject(); // Google Account ID
//
//        // Kiểm tra nếu tài khoản Google đã tồn tại
//        Optional<User> optionalUser = userRepository.findByGoogleAccountId(Integer.parseInt(googleAccountId));
//        if (optionalUser.isPresent()) {
//            return optionalUser.get(); // Trả về user nếu đã tồn tại
//        }
//
//        // Nếu chưa tồn tại, tạo user mới và lưu vào DB
//        Role defaultRole = roleRepository.findByName("USER")
//                .orElseThrow(() -> new RuntimeException("Default role not found"));
//
//        User newUser = User.builder()
//                .fullName(name)
//                .googleAccountId(Integer.parseInt(googleAccountId)) // Chuyển đổi sang int
//                .role(defaultRole) // Quyền mặc định, ví dụ: USER
//                .build();
//
//        return userRepository.save(newUser);
//    }
}
