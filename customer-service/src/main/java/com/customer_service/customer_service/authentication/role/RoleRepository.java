package com.customer_service.customer_service.authentication.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String roleName);

    @Query(nativeQuery = true,value = "Select count(*) from role")
    Integer noOfRoles();
}
