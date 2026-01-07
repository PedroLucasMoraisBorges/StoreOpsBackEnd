package com.store_ops_backend.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.UserRepository;


@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.repository.findBylogin(username);
    }

    public User loadUserById(String id) throws UsernameNotFoundException {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
}
