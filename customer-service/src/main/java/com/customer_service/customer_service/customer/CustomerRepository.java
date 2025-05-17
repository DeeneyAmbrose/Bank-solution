package com.customer_service.customer_service.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query(value = "SELECT * FROM customer c " +
            "WHERE (c.first_name ILIKE '%' || :keyword || '%' " +
            "OR c.last_name ILIKE '%' || :keyword || '%') " +
            "AND c.created_at >= COALESCE(:startDate, c.created_at) " +
            "AND c.created_at <= COALESCE(:endDate, c.created_at) " +
            "ORDER BY c.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM customer c " +
                    "WHERE (c.first_name ILIKE '%' || :keyword || '%' " +
                    "OR c.last_name ILIKE '%' || :keyword || '%') " +
                    "AND c.created_at >= COALESCE(:startDate, c.created_at) " +
                    "AND c.created_at <= COALESCE(:endDate, c.created_at)",
            nativeQuery = true)
    Page<Customer> findByKeywordAndDateRange(
            @Param("keyword") String keyword,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);



}
