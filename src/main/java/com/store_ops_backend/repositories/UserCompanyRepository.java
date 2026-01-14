package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.UserCompany;

public interface UserCompanyRepository extends JpaRepository<UserCompany, String> {
    @Query("""
        select uc
        from UserCompany uc
        where uc.id.userId = :userId
    """)
    List<UserCompany> findCompaniesByUserId(@Param("userId") String userId);

}
