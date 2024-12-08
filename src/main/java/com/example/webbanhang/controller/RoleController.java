package com.example.webbanhang.controller;

import com.example.webbanhang.models.Role;
import com.example.webbanhang.provider.JwtTokenProvider;
import com.example.webbanhang.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public RoleController(JwtTokenProvider jwtTokenProvider, RoleService roleService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleService = roleService;
    }

    @RequestMapping("/api/v1/roles")
    public ResponseEntity<List<Role>> getRoles(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Lấy token từ header
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);  // Nếu token không hợp lệ
        }

        // Lấy roles nếu token hợp lệ
        List<Role> roles = roleService.getRoles();
        return ResponseEntity.ok(roles);
    }

}
