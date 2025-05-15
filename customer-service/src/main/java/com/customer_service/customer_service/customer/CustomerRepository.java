package com.customer_service.customer_service.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "SELECT * FROM customer c \n" +
            "WHERE  c.first_name LIKE CONCAT ('%', :keyword, '%')\n" +
            "   OR c.last_name LIKE CONCAT ('%', :keyword, '%')\n" +
            "   OR c.created_at LIKE CONCAT ('%', :keyword, '%');", nativeQuery = true)
    List<Customer> findByKeyword(String keyword);
}
