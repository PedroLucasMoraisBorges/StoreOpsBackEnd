package com.store_ops_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.CompanyResponseDTO;
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
    private PaymentMethodService paymentMethodService;

    @Autowired
    private AuthenticationService authenticationService;


    public CompanyResponseDTO createCompany(CreateCompanyDTO data, String userId) {
        Company newCompany = new Company(data.name(), data.type(), data.address(), data.phone(), data.teamSize(), data.formOfService(),
            data.notifications().newOrder(), data.notifications().weeklyReports(),
            data.notifications().email(), data.notifications().accounts());
        repository.save(newCompany);
        User user = authenticationService.loadUserById(userId);
        
        userCompanyService.createUserCompany(user, newCompany, "ADMIN");

        paymentMethodService.createCompanyPaymentMethods(newCompany, data.paymentMethodIds());

        System.out.println(newCompany.getId());
        var paymentMethods = paymentMethodService.getCompanyPaymentMethods(newCompany.getId());
        return new CompanyResponseDTO(newCompany, paymentMethods);
    }
}
