package com.store_ops_backend.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.UserCompanyRepository;

@Component
public class AuthorizationHelper {

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    public void assertUserBelongsToCompany(User user, String companyId) {
        userCompanyRepository.findByCompanyIdAndUserId(companyId, user.getId())
            .orElseThrow(() -> new AccessDeniedException("Acesso negado: você não pertence a esta empresa."));
    }
}
