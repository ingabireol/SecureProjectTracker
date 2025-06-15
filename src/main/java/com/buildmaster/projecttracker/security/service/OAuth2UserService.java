package com.buildmaster.projecttracker.security.service;

import com.buildmaster.projecttracker.model.user.AuthProvider;
import com.buildmaster.projecttracker.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {
    
    private final AuthService authService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Loading OAuth2 user from provider: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user");
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = getAuthProvider(registrationId);
        
        // Extract user info based on provider
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());
        
        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        // Get or create user
        User user = authService.getOrCreateOAuth2User(
            userInfo.getEmail(),
            userInfo.getName(),
            userInfo.getId(),
            provider
        );
        
        log.info("OAuth2 user processed successfully: {}", user.getUsername());
        
        // Return custom OAuth2User implementation
        return new CustomOAuth2User(user, oauth2User.getAttributes());
    }
    
    private AuthProvider getAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}

// OAuth2 User Info interface
interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
}

class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, java.util.Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GitHubOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}

// Google OAuth2 User Info
class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final java.util.Map<String, Object> attributes;
    
    public GoogleOAuth2UserInfo(java.util.Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }
    
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
    
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    
    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}

// GitHub OAuth2 User Info
class GitHubOAuth2UserInfo implements OAuth2UserInfo {
    private final java.util.Map<String, Object> attributes;
    
    public GitHubOAuth2UserInfo(java.util.Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }
    
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
    
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    
    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}

// Custom OAuth2User implementation
class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final java.util.Map<String, Object> attributes;
    
    public CustomOAuth2User(User user, java.util.Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    
    @Override
    public java.util.Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }
    
    @Override
    public String getName() {
        return user.getUsername();
    }
    
    public User getUser() {
        return user;
    }
}