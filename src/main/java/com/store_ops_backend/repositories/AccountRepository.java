package com.store_ops_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
    @Query("""
        select a
        from accounts a
        where a.people.id = :personId
        and a.company.id = :companyId
    """)
    Optional<Account> findByPersonIdAndCompanyId(
        @Param("personId") String personId,
        @Param("companyId") String companyId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from accounts a
        where a.company.id = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);

}
