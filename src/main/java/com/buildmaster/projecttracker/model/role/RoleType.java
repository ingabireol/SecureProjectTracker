package com.buildmaster.projecttracker.model.role;

// Enum for predefined roles
public enum RoleType {
    ROLE_ADMIN("ROLE_ADMIN", "System administrator with full access"),
    ROLE_MANAGER("ROLE_MANAGER", "Project manager who can create and manage projects"),
    ROLE_DEVELOPER("ROLE_DEVELOPER", "Developer who can update assigned tasks"),
    ROLE_CONTRACTOR("ROLE_CONTRACTOR", "External contractor with read-only access");

    private final String roleName;
    private final String description;

    RoleType(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}
