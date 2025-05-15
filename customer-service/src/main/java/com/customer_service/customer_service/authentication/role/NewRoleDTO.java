package com.customer_service.authentication.role;

import com.customer_service.authentication.enums.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class NewRoleDTO {
    String name;
    Set<Permission> permissions;
}
