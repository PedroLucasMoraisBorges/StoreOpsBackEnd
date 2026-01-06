package com.store_ops_backend.models.dtos;

import com.store_ops_backend.models.entities.UserRole;

public record RegisterDTO(String email, String password, UserRole role) {
    
}
