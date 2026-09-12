package com.thecodecompanyinc.social_media_service.entity;

public enum Role {
    CLIENT,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
