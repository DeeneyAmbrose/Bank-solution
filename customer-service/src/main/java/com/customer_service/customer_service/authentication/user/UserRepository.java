package com.customer_service.customer_service.authentication.user;


import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(nativeQuery = true, value = "Select count(*) from user join role r on r.id = user.role_id where r.name=:roleName")
    Integer adminCount(@Param("roleName") String roleName);

    Optional<User>  findByEmail(String email);
    Optional<User> findByUsername(String username);
}
