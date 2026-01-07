package com.store_ops_backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.repositories.UserCompanyRepository;

@Service
public class UserCompanyService {
    @Autowired
    UserCompanyRepository repository;

    public void createUserCompany(User user, Company company, String role) {
        UserCompany userCompany = new UserCompany(user, company, role);
        repository.save(userCompany);
    }

    public List<Company> getCompaniesByUserId(String userId) {
        List<Company> companies = repository
            .findByUser_Id(userId)
            .stream()
            .map(UserCompany::getCompany)
            .toList();

        return companies;
    }
}
