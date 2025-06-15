package com.buildmaster.projecttracker.security.service;

import com.buildmaster.projecttracker.model.role.Role;
import com.buildmaster.projecttracker.model.role.RoleType;
import com.buildmaster.projecttracker.model.user.User;
import com.buildmaster.projecttracker.model.user.AuthProvider;
import com.buildmaster.projecttracker.repository.RoleRepository;
import com.buildmaster.projecttracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run before DataInitializationService
public class RoleInitializationService implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.email:admin@projecttracker.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing roles and default admin user...");
        
        initializeRoles();
        initializeDefaultAdmin();
        
        log.info("Role and admin initialization completed!");
    }
    
    private void initializeRoles() {
        log.info("Creating default roles...");
        
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType.getRoleName())) {
                Role role = new Role(roleType.getRoleName(), roleType.getDescription());
                roleRepository.save(role);
                log.info("Created role: {} - {}", role.getName(), role.getDescription());
            } else {
                log.debug("Role already exists: {}", roleType.getRoleName());
            }
        }
    }
    
    private void initializeDefaultAdmin() {
        log.info("Creating default admin user...");
        
        if (!userRepository.existsByUsername(adminUsername)) {
            // Create default admin user
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setProvider(AuthProvider.LOCAL);
            adminUser.setEnabled(true);
            adminUser.setAccountNonExpired(true);
            adminUser.setAccountNonLocked(true);
            adminUser.setCredentialsNonExpired(true);
            
            // Assign ADMIN role
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            adminUser.addRole(adminRole);
            
            userRepository.save(adminUser);
            
            log.info("Default admin user created:");
            log.info("  Username: {}", adminUsername);
            log.info("  Email: {}", adminEmail);
            log.info("  Password: {}", adminPassword);
            log.warn("IMPORTANT: Change the default admin password after first login!");
            
        } else {
            log.info("Default admin user already exists: {}", adminUsername);
        }
    }
}