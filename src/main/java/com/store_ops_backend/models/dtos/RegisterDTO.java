package com.store_ops_backend.models.dtos;

import com.store_ops_backend.models.entities.UserRole;

public record RegisterDTO(String login, String name, String password, UserRole role) {
	
}
