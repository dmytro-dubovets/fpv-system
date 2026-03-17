package ua.fpv.entity.response;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Permission {

    FPVPILOT_READ("fpvpilot:read"),
    FPVPILOT_WRITE("fpvpilot:write"),
    FPVREPORT_READ("fpvreport:read"),
    FPVREPORT_WRITE("fpvreport:write"),
    USER_READ("user:read"),
    USER_WRITE("user:write");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public static boolean isValid(String authority) {
        return Arrays.stream(Permission.values())
                .anyMatch(p -> p.getPermission().equals(authority));
    }
}