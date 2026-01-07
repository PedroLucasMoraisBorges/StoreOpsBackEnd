package com.store_ops_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.CreateCompanyDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.CompanyRepository;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository repository;

    @Autowired 
    private UserCompanyService userCompanyService;

    @Autowired
    private AuthenticationService authenticationService;


    public Company createCompany(CreateCompanyDTO data, String userId) {
        Company newCompany = new Company(data.name());
        repository.save(newCompany);
        User user = authenticationService.loadUserById(userId);
        
        System.out.println("Associating user " + user.getLogin() + " with company " + newCompany.getName() + " as ADMIN.");
        userCompanyService.createUserCompany(user, newCompany, "ADMIN");

        return newCompany;
    }
}
