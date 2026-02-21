package com.smartprocure.dto.auth;

public class LoginResponse {

    private String tokenType = "Bearer";
    private String accessToken;
    private long expiresInSeconds;
    private String role;
    private String fullName;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, long expiresInSeconds, String role, String fullName) {
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
        this.role = role;
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
