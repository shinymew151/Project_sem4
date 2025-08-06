// UserService.java
package com.ai.video.FacelessVideo.service;

import com.ai.video.FacelessVideo.entity.User;
import com.ai.video.FacelessVideo.entity.Role;
import com.ai.video.FacelessVideo.entity.UserWallet;
import com.ai.video.FacelessVideo.repository.UserRepository;
import com.ai.video.FacelessVideo.repository.RoleRepository;
import com.ai.video.FacelessVideo.repository.UserWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserWalletRepository userWalletRepository;
    
    @Autowired
    private AuditService auditService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${app.default.admin.username:admin}")
    private String defaultAdminUsername;
    
    @Value("${app.default.admin.email:admin@facelessvideo.com}")
    private String defaultAdminEmail;
    
    @Value("${app.default.admin.password:changeme123}")
    private String defaultAdminPassword;

    @PostConstruct
    public void initializeDefaultAdmin() {
        try {
            // Create default admin user if not exists
            if (!userRepository.existsByUsername(defaultAdminUsername)) {
                createDefaultAdminUser();
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize default admin: " + e.getMessage());
        }
    }
    
    private void createDefaultAdminUser() {
        // Create admin role if not exists
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });
        
        // Create default admin user
        User admin = new User();
        admin.setUsername(defaultAdminUsername);
        admin.setEmail(defaultAdminEmail);
        admin.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
        admin.setRoles(Set.of(adminRole));
        admin = userRepository.save(admin);
        
        // Create wallet for admin
        UserWallet wallet = new UserWallet();
        wallet.setUser(admin);
        wallet.setBalancePoints(1000); // Give admin 1000 points to start
        userWalletRepository.save(wallet);
        
        auditService.logAction("USER_CREATED", admin.getId(), "User", admin.getId(), 
                "Default admin user created");
        
        System.out.println("✅ Default admin user created: " + defaultAdminUsername);
    }
    
    public User createUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        
        // Assign default user role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("USER");
                    return roleRepository.save(role);
                });
        
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        
        // Create wallet for new user
        UserWallet wallet = new UserWallet();
        wallet.setUser(user);
        wallet.setBalancePoints(50); // Give new users 50 points to start
        userWalletRepository.save(wallet);
        
        auditService.logAction("USER_CREATED", user.getId(), "User", user.getId(), 
                "New user registered");
        
        return user;
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPasswordHash());
    }
}

