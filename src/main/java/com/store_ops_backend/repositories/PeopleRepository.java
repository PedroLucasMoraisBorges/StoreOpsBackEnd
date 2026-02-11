package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("""
        select p
        from people p
        where p.company.id = :companyId
    """)
    List<People> findByCompanyIdAndType(
        @Param("companyId") String companyId
    );

    @Query("""
        select p
        from people p
        where p.company.id = :companyId
        and p.id = :personId
    """)
    Optional<People> findByCompanyIdAndPersonIdAndType(
        @Param("companyId") String companyId,
        @Param("personId") String personId
    );

    @Query("""
        select p
        from people p
        where p.company.id = :companyId
        and p.user.id = :userId
        and p.type = :type
    """)
    Optional<People> findByCompanyIdAndEmployeeIdAndType(
        @Param("companyId") String companyId,
        @Param("userId") String userId,
        @Param("type") String type
    );

    @Query("""
        select p
        from people p
        where p.company.id = :companyId
        and p.user.id = :userId
        and p.type = :type
    """)
    Optional<People> findByUserIdAndCompanyIdAndType(
        @Param("userId") String userId,
        @Param("companyId") String companyId,
        @Param("type") String type
    );

    @Modifying
    @Transactional
    @Query("""
        delete from people p
        where p.company.id = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);
}
