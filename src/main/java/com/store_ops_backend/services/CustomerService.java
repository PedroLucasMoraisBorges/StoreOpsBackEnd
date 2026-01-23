package com.store_ops_backend.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateCustomerDTO;
import com.store_ops_backend.models.dtos.CustomerResponseDTO;
import com.store_ops_backend.models.dtos.UpdateCustomerDTO;
import com.store_ops_backend.models.entities.Account;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.AccountRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.PeopleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService {
    private static final String CUSTOMER_TYPE = "CLIENT";
    private static final String EMPLOYEE_TYPE = "EMPLOYEE";

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public CustomerResponseDTO createCustomer(CreateCustomerDTO data, String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        String name = data.name() == null || data.name().isBlank() ? "Cliente" : data.name();
        People people = new People(
            name,
            CUSTOMER_TYPE,
            company,
            null,
            data.address(),
            data.contact(),
            data.isActive()
        );
        peopleRepository.save(people);

        Account account = new Account(null, "OPEN", OffsetDateTime.now(), null, people, company);
        accountRepository.save(account);

        return toResponse(people);
    }

    @Transactional
    public People createCustomerForEmployee(Company company, User user, String name) {
        return peopleRepository
            .findByUserIdAndCompanyIdAndType(user.getId(), company.getId(), CUSTOMER_TYPE)
            .orElseGet(() -> {
                People people = new People(
                    name,
                    CUSTOMER_TYPE,
                    company,
                    user,
                    null,
                    null,
                    true
                );
                peopleRepository.save(people);
                Account account = new Account(null, "OPEN", OffsetDateTime.now(), null, people, company);
                accountRepository.save(account);
                return people;
            });
    }

    public List<CustomerResponseDTO> getAllCustomers(String companyId) {
        return peopleRepository
            .findByCompanyIdAndType(companyId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public CustomerResponseDTO getCustomerById(String companyId, String customerId) {
        People people = peopleRepository
            .findByCompanyIdAndPersonIdAndType(companyId, customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        return toResponse(people);
    }

    @Transactional
    public CustomerResponseDTO updateCustomer(String companyId, String customerId, UpdateCustomerDTO data) {
        People people = peopleRepository
            .findByCompanyIdAndPersonIdAndType(companyId, customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        people.update(data.name(), data.address(), data.contact(), data.isActive());
        return toResponse(people);
    }

    public People findPersonByCustomerOrEmployee(
            String companyId,
            String personOrEmployeeId
    ) {
        return peopleRepository
            .findByCompanyIdAndPersonIdAndType(companyId, personOrEmployeeId)
            .or(() ->
                peopleRepository.findByCompanyIdAndEmployeeIdAndType(
                    companyId,
                    personOrEmployeeId,
                    EMPLOYEE_TYPE
                )
            )
            .orElseThrow(() ->
                new EntityNotFoundException(
                    "Person not found as CUSTOMER nor EMPLOYEE for id=" + personOrEmployeeId
                )
            );
    }


    public Account findCustomerAccount(String companyId, String personId) {
        return accountRepository
            .findByPersonIdAndCompanyId(personId, companyId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
    }


    private CustomerResponseDTO toResponse(People people) {
        return new CustomerResponseDTO(
            people.getId(),
            people.getName(),
            people.getAddress(),
            people.getContact(),
            people.getIs_active()
        );
    }
}
