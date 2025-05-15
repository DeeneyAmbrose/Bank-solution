package com.customer_service.customer_service.authentication.role;

import com.customer_service.authentication.enums.Permission;
import com.customer_service.authentication.enums.Status;
import com.customer_service.customer_service.utilities.EntityResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Set<Permission> getAccessRights() {
        return Arrays.stream(Permission.values()).collect(Collectors.toSet());
    }




    public void createRole(@NonNull String name, @NonNull Set<Permission> permissions) {
        try {
            AtomicBoolean resource = new AtomicBoolean(false);
            AtomicReference<Role> role = new AtomicReference<>(new Role());
            role.get().setName(name);
            role.get().setPermissions(permissions);
            role.get().setEnabledFlag('Y');
            role.get().setStatus(Status.ACTIVE);
            role.set(this.roleRepository.save(role.get()));
            resource.set(true);
            log.info(String.format("Role created [ %s ]", role.get()));

            resource.get();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


}
