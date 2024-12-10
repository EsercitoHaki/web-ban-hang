package com.example.webbanhang.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 255)
    private String token;

    @Column(name = "token_type", length = 50)
    private String tokenType;

//    @Column(name = "refresh_token", length = 255)
//    private String refreshToken;

    @Column(name = "expiration_date", length = 255)
    private LocalDateTime expirationDate;

//    @Column(name = "refresh_expiration_date")
//    private LocalDateTime refreshExpirationDate;

    private boolean revoked;

    private boolean expired;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}