package com.store_ops_backend.models.entities;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "users")
// Nome explícito da tabela.
// Evita dependência de convenções implícitas e problemas futuros.

@Entity(name = "users")
// Marca a classe como entidade JPA.
// O name alinhado com a tabela facilita leitura e manutenção.

@Getter
// Gera getters automaticamente.
// Entidade imutável externamente (sem setters públicos).

@NoArgsConstructor
// Obrigatório para o JPA instanciar a entidade via reflexão.

@AllArgsConstructor
// Facilita criação da entidade em testes ou cenários controlados.

@EqualsAndHashCode(of = "id")
// Identidade da entidade baseada apenas no ID.
// Evita bugs sutis em coleções e no contexto de persistência.

public class User implements UserDetails {
    // Implementar UserDetails integra diretamente com Spring Security.
    // Essa classe passa a ser o "usuário oficial" do sistema.

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // UUID como chave primária:

    private String id;
    private String email;
    private String password;
    private UserRole role;
    // 🧠 Enum que define o perfil do usuário (USER, ADMIN, etc).
    // Centraliza a lógica de autorização.

    public User(String email, String password, UserRole role) {
        // Construtor de conveniência.
        // Ideal para cadastro de novos usuários,
        // sem expor ou manipular o ID manualmente.
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    @Override
    public String getUsername() {
        // Define qual campo será usado no processo de autenticação.
        // Aqui, login = email.
        return this.email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Define as permissões do usuário.
        // Spring Security trabalha com "authorities", não diretamente com roles.

        if (this.role == UserRole.ADMIN)
            // ADMIN herda USER.
            // Decisão estratégica: evita duplicação de regras de acesso.
            return List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
        else
            // Usuário padrão com acesso básico.
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
