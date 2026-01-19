package com.store_ops_backend.models.dtos;

public record CreateEmployeeDTO (String name, String login, String password, String role, String position) {
    
}
