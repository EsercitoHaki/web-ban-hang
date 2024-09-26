package com.example.webbanhang.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {
    @Column(name = "created_at")
    private LocalDateTime createAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}

