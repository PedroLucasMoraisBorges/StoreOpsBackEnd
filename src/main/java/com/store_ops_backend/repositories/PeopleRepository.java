package com.store_ops_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.People;

public interface PeopleRepository extends JpaRepository<People, String> {
    @Query("""
        select p
        from people p
        where p.user.id = :userId
        and p.company.id = :companyId
    """)
    Optional<People> findByUserIdAndCompanyId(
        @Param("userId") String userId,
        @Param("companyId") String companyId
    );
}
