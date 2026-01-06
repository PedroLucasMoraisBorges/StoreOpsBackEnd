package com.store_ops_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.store_ops_backend.models.entities.User;


public interface UserRepository extends JpaRepository<User, String> {
    UserDetails findByEmail(String email);
}
