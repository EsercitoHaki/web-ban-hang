package com.example.webbanhang.configurations;

import com.example.webbanhang.filters.JwtTokenFilter;
import com.example.webbanhang.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


import static org.springframework.http.HttpMethod.*;

@Configuration
//@EnableMethodSecurity
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebMvc
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    @Value("${api.prefix}")
    private String apiPrefix;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http
//                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(requests -> {
                requests
                        .requestMatchers(
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/login", apiPrefix)
                        )
                        .permitAll()

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                        .requestMatchers(GET,
                                String.format("%s/products/recommendations/**", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/comments/**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/comments**", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/comments/{parentId}/reply", apiPrefix)).permitAll()

                        .requestMatchers(
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/login", apiPrefix)
                        ).permitAll()

                        .requestMatchers(PUT,
                                String.format("%s/users/admin/details/**", apiPrefix)).hasRole("ADMIN")

                        .requestMatchers(PUT,
                                String.format("%s/users/block/**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/roles**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/categories**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/categories/**", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(PUT,
                                String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/categories/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/products**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/products/**", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/products/images/*", apiPrefix)).permitAll()

                        .requestMatchers(POST,
                                String.format("%s/products**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(PUT,
                                String.format("%s/products/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/products/**", apiPrefix)).hasAnyRole(Role.ADMIN)

                        .requestMatchers(POST,
                                String.format("%s/orders/**", apiPrefix)).hasAnyRole(Role.USER)

                        .requestMatchers(GET,
                                String.format("%s/orders/get-orders-by-keyword", apiPrefix)).permitAll()

                        .requestMatchers(GET,
                                String.format("%s/orders/**", apiPrefix)).permitAll()

                        .requestMatchers(PUT,
                                String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/orders/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(POST,
                                String.format("%s/order_details/**", apiPrefix)).hasAnyRole(Role.USER)

                        .requestMatchers(GET,
                                String.format("%s/order_details/**", apiPrefix)).permitAll()

                        .requestMatchers(PUT,
                                String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(DELETE,
                                String.format("%s/order_details/**", apiPrefix)).hasRole(Role.ADMIN)

                        .requestMatchers(GET,
                                String.format("%s/healthcheck/**", apiPrefix)).permitAll()


                        .anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable);
        http.securityMatcher(String.valueOf(EndpointRequest.toAnyEndpoint()));
        return http.build();
    }
}
