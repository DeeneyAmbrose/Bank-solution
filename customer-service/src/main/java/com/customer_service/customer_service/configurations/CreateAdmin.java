package com.customer_service.customer_service.configurations;

import com.customer_service.authentication.enums.Status;

import com.customer_service.customer_service.authentication.role.Role;
import com.customer_service.customer_service.authentication.role.RoleRepository;
import com.customer_service.customer_service.authentication.role.RoleService;
import com.customer_service.customer_service.authentication.user.User;
import com.customer_service.customer_service.authentication.user.UserRepository;
import com.customer_service.customer_service.utilities.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CreateAdmin implements ApplicationRunner {

    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public CreateAdmin(RoleService roleService, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args)  {
        int roles = roleRepository.noOfRoles();

        if(roles < 1) {
            addAdminRole();
        }
        addAdmin();
    }

    void addAdminRole() {
        log.info("creating an admin role on "+ LocalDateTime.now());
        roleService.createRole("SYSTEM_ADMIN", roleService.getAccessRights());
        log.info("Admin role created at "+LocalDateTime.now());
    }

    void addAdmin() {
        try {
            Integer admin = userRepository.adminCount("SYSTEM_ADMIN");

            if(admin >= 1) {
                log.info("System admin already exists .....");
            } else {
                log.info("creating system admin ....");
                User user = new User();
                Role adminRole = roleRepository.findByName("SYSTEM_ADMIN").orElseThrow(()-> new ResourceNotFoundException("Role with name ROLE_ADMIN not found"));

                user.setEmail("admin.system@info");
                user.setFirstname("Super");
                user.setLastname("Admin");
                user.setPassword(passwordEncoder.encode("test"));
                user.setEnabledFlag('Y');
                user.setStatus(Status.ACTIVE);
                user.setRole(adminRole);
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
