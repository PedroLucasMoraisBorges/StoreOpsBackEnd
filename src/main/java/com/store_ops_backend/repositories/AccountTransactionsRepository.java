package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.AccountTransactions;

public interface AccountTransactionsRepository extends JpaRepository<AccountTransactions, String> {
    @Query("""
        select t
        from account_transactions t
        where t.account.id = :accountId
        order by t.created_at desc
    """)
    List<AccountTransactions> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") String accountId);

    @Query("""
        select t
        from account_transactions t
        where t.account.company.id = :companyId
        and t.origin = 'CUSTOMER_DEBIT'
        and t.created_at between :dateFrom and :dateTo
        order by t.created_at desc
    """)
    List<AccountTransactions> findFiadoByCompanyAndCreatedAtBetween(
        @Param("companyId") String companyId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select t
        from account_transactions t
        where t.account.company.id = :companyId
        and t.origin = 'CUSTOMER_DEBIT'
        and t.user.id = :userId
        and t.created_at between :dateFrom and :dateTo
        order by t.created_at desc
    """)
    List<AccountTransactions> findFiadoByCompanyAndUserIdAndCreatedAtBetween(
        @Param("companyId") String companyId,
        @Param("userId") String userId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select t
        from account_transactions t
        where t.account.company.id = :companyId
        and t.origin = 'CUSTOMER_PAYMENT'
        and t.created_at between :dateFrom and :dateTo
        order by t.created_at desc
    """)
    List<AccountTransactions> findPaymentsByCompanyAndCreatedAtBetween(
        @Param("companyId") String companyId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select t
        from account_transactions t
        where t.account.company.id = :companyId
        order by t.created_at desc
    """)
    List<AccountTransactions> findByCompanyId(@Param("companyId") String companyId);

    @Modifying
    @Transactional
    @Query("""
        delete from account_transactions t
        where t.account.company.id = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);
}
