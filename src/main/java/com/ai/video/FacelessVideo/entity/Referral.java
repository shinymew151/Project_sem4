package com.ai.video.FacelessVideo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Referral")
public class Referral {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "referrer_id")
    private User referrer;
    
    @ManyToOne
    @JoinColumn(name = "referred_user_id")
    private User referredUser;
    
    @Column(name = "referral_code")
    private String referralCode;
    
    @Column(name = "bonus_points")
    private Integer bonusPoints;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
