package com.store_ops_backend.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeDTO(
    @NotBlank(message = "Nome é obrigatório") String name,
    @NotBlank(message = "Login é obrigatório") String login,
    @NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String password,
    @NotBlank(message = "Role é obrigatória") String role,
    String position
) {}
