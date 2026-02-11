package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.UserCompany;

public interface UserCompanyRepository extends JpaRepository<UserCompany, String> {
    @Query("""
        select uc
        from UserCompany uc
        where uc.id.userId = :userId
    """)
    List<UserCompany> findCompaniesByUserId(@Param("userId") String userId);

    @Query("""
        select uc
        from UserCompany uc
        where uc.id.companyId = :companyId
    """)
    List<UserCompany> findUsersByCompanyId(@Param("companyId") String companyId);

    @Query("""
        select uc
        from UserCompany uc
        where uc.id.companyId = :companyId
        and uc.id.userId = :userId
    """)
    Optional<UserCompany> findByCompanyIdAndUserId(
        @Param("companyId") String companyId,
        @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from UserCompany uc
        where uc.id.companyId = :companyId
        and uc.id.userId = :userId
    """)
    void deleteByCompanyIdAndUserId(
        @Param("companyId") String companyId,
        @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from UserCompany uc
        where uc.id.companyId = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);

    @Modifying
    @Transactional
    @Query("""
        update UserCompany uc
        set uc.status = case
                when uc.status = true then false
                else true
        end
        where uc.id.companyId = :companyId
        and uc.id.userId = :userId
    """)
    void updateStatusByCompanyIdAndUserId(
        @Param("companyId") String companyId,
        @Param("userId") String userId
    );


}
