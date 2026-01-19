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

    public void createUserCompany(User user, Company company, String role, String position) {
        UserCompany userCompany = new UserCompany(user, company, role, position);
        repository.save(userCompany);
    }

    public UserCompany createUserCompanyWithPosition(User user, Company company, String role, String position) {
        UserCompany userCompany = new UserCompany(user, company, role, position);
        return repository.save(userCompany);
    }

    public List<Company> getCompaniesByUserId(String userId) {
        List<Company> companies = repository
            .findCompaniesByUserId(userId)
            .stream()
            .map(UserCompany::getCompany)
            .toList();
        

        return companies;
    }

    public List<UserCompany> getUsersByCompanyId(String companyId) {
        return repository.findUsersByCompanyId(companyId);
    }

    public UserCompany getUserCompany(String companyId, String userId) {
        return repository
            .findByCompanyIdAndUserId(companyId, userId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public void deleteUserCompany(String companyId, String userId) {
        repository.deleteByCompanyIdAndUserId(companyId, userId);
    }

    public void updateUserCompanyStatus(String companyId, String userId) {
        repository.updateStatusByCompanyIdAndUserId(companyId, userId);
    }

    public UserCompany saveUserCompany(UserCompany userCompany) {
        return repository.save(userCompany);
    }
}
