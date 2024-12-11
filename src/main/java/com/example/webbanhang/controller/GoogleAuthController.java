package com.example.webbanhang.controller;


import com.example.webbanhang.dtos.TokenRequest;
import com.example.webbanhang.models.User;
import com.example.webbanhang.services.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;



@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GoogleAuthController {

    @Autowired
    private UserService userService;  // Giả sử bạn đã tạo UserService để xử lý người dùng

    @PostMapping("/login_google")
    @CrossOrigin(origins = "http://localhost:4200")
    public String loginWithGoogle(@RequestBody TokenRequest tokenRequest) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + tokenRequest.getToken();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Gửi yêu cầu để xác thực token Google
            JsonNode userJson = restTemplate.getForObject(url, JsonNode.class);
            if (userJson == null) {
                return "Lỗi xác thực token Google.";
            }

            String googleAccountId = userJson.get("sub").asText();
            String email = userJson.get("email").asText();
            String fullName = userJson.get("name").asText();
//            String profilePicture = userJson.get("picture").asText();

            // Kiểm tra người dùng trong cơ sở dữ liệu
            User existingUser = userService.findByGoogleAccountId(googleAccountId);
            if (existingUser != null) {
                return "Đăng nhập thành công 123!";
            } else {
                // Tạo tài khoản mới nếu người dùng chưa có
                userService.createGoogleUser(googleAccountId, fullName, email);
                return "Tài khoản mới đã được tạo!";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi xác thực token Google.";
        }
    }
}
