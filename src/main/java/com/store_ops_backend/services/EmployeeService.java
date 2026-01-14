package com.store_ops_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.CreateEmployeeDTO;

@Service
public class EmployeeService {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserCompanyService userCompanyService;

    public void createEmployee(CreateEmployeeDTO data, String companyId) {
        
    }
}
