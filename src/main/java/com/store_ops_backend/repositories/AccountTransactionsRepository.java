package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.AccountTransactions;

public interface AccountTransactionsRepository extends JpaRepository<AccountTransactions, String> {
    @Query("""
        select t
        from account_transactions t
        where t.account.id = :accountId
        order by t.created_at desc
    """)
    List<AccountTransactions> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") String accountId);
}
