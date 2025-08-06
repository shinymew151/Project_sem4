package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Table(name = "User")
public class User {
    @SuppressWarnings("deprecation")
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UserRole",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}