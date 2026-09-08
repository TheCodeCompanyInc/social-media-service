package com.thecodecompanyinc.social_media_service.entity;

public enum Role {
    CLIENT,
    ADMIN,
    VEHICLE;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
