package com.buildmaster.projecttracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT authentication response")
public class JwtResponseDto {
    
    @Schema(description = "JWT access token")
    private String accessToken;
    
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "Token expiration time in milliseconds")
    private Long expiresIn;
    
    @Schema(description = "User information")
    private UserResponseDto user;
    
    public JwtResponseDto(String accessToken, Long expiresIn, UserResponseDto user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
