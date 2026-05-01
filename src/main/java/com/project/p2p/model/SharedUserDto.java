package com.project.p2p.model;

public class SharedUserDto {
    private String userId;
    private String displayName;

    public SharedUserDto(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
