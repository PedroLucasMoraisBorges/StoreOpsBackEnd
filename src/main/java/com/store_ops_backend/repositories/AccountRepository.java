package com.store_ops_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
}
