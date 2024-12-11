package com.example.webbanhang.services;

import java.io.IOException;
import java.util.Map;

public interface IAuthService {
    String generateAuthUrl(String loginType);
    Map<String, Object> authenticateAndFetchProfile(String code, String loginType) throws IOException;
}
