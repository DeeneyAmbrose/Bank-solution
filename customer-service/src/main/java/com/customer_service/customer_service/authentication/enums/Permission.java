package com.customer_service.authentication.enums;

import lombok.Getter;

@Getter
public enum Permission {
//roles rights
    CREATE_ROLE("role:create"),
    UPDATE_ROLE("role:update"),
    DEACTIVATE_ROLE("role:deactivate"),
    ASSIGN_ROLE("role:assign"),
    DELETE_ROLE("role:delete"),
    VIEW_ROLE("role:view"),

//users rights
   CREATE_USER("user:create"),
   VIEW_USER("user:view"),
   ACTIVATE_USER("user:activate"),
   DEACTIVATE_USER("user:deactivate"),
    UPDATE_USER("user:update"),
    DELETE_USER("user:delete");


    private final String permission;
    Permission(String permission) {
        this.permission = permission;
    }
}
