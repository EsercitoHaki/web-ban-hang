package com.example.webbanhang.services;

import com.example.webbanhang.models.Token;
import com.example.webbanhang.models.User;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
