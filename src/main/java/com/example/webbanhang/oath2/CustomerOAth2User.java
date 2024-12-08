package com.example.webbanhang.oath2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Map;

public class CustomerOAth2User implements OAuth2User {
    private OAuth2User auth2User;

    public CustomerOAth2User(OAuth2User oAuth2User) {
        this.auth2User = oAuth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return auth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return auth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return auth2User.getAttribute("name");
    }

    public String getFullName() {
        return auth2User.getAttribute("name");
    }
}
