package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.UserCompany;

public interface UserCompanyRepository extends JpaRepository<UserCompany, String> {
    public List<UserCompany> findByUser_Id(String userId);
}
