package com.store_ops_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CompanyResponseDTO;
import com.store_ops_backend.models.dtos.CreateCompanyDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.CompanyPaymentMethodsRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.AccountRepository;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.PeopleRepository;
import com.store_ops_backend.repositories.UserCompanyRepository;

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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountTransactionsRepository accountTransactionsRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private CompanyPaymentMethodsRepository companyPaymentMethodsRepository;

    public CompanyResponseDTO createCompany(CreateCompanyDTO data, String userId) {
        Company newCompany = new Company(data.name(), data.type(), data.address(), data.phone(), data.teamSize(), data.formOfService(),
            data.notifications().newOrder(), data.notifications().weeklyReports(),
            data.notifications().email(), data.notifications().accounts());
        repository.save(newCompany);
        User user = authenticationService.loadUserById(userId);
        
        userCompanyService.createUserCompany(user, newCompany, "ADMIN", "ADMIN");

        paymentMethodService.createCompanyPaymentMethods(newCompany, data.paymentMethodIds());

        return getCompanyById(newCompany.getId());
    }

    public CompanyResponseDTO getCompanyById(String companyId) {
        Company company = repository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
        var paymentMethods = paymentMethodService.getCompanyPaymentMethods(companyId);
        var allPaymentMethods = paymentMethodService.getAllPaymentMethods();
        return new CompanyResponseDTO(company, paymentMethods, allPaymentMethods);
    } 

    public CompanyResponseDTO updateCompany(String companyId, CreateCompanyDTO data) {
        Company company = repository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));

        company.update(
            data.name(),
            data.type(),
            data.address(),
            data.phone(),
            data.teamSize(),
            data.formOfService(),
            data.notifications().newOrder(),
            data.notifications().weeklyReports(),
            data.notifications().email(),
            data.notifications().accounts()
        );

        paymentMethodService.syncCompanyPaymentMethods(
            company,
            data.paymentMethodIds()
        );

        repository.save(company);
        return getCompanyById(company.getId());
    }

    public List<CompanyResponseDTO> getAllUserCompanies(String userId, String filter) {
        List<Company> companies = userCompanyService.getCompaniesByUserId(userId);
        return companies.stream().map(company -> {
            return getCompanyById(company.getId());
        }).toList();
    }

    @Transactional
    public void deleteCompany(String companyId, String userId) {
        UserCompany userCompany = userCompanyService.getUserCompany(companyId, userId);
        if (userCompany.getRole() == null || !userCompany.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("User not allowed");
        }

        orderRepository.deleteByCompanyId(companyId);
        accountTransactionsRepository.deleteByCompanyId(companyId);
        accountRepository.deleteByCompanyId(companyId);
        peopleRepository.deleteByCompanyId(companyId);
        userCompanyRepository.deleteByCompanyId(companyId);
        companyPaymentMethodsRepository.deleteByCompanyId(companyId);
        repository.deleteById(companyId);
    }
}
