package com.account_service.account_service.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountsRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a FROM Account a " +
            "WHERE (:iban IS NULL OR a.iban LIKE %:iban%) " +
            "AND (:bicSwift IS NULL OR a.bicSwift LIKE %:bicSwift%) "
           )
    Page<Account> searchAccounts(@Param("iban") String iban,
                                 @Param("bicSwift") String bicSwift,
                                 Pageable pageable);

    Optional<Account> findByIban(String iban);
}
