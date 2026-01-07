package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CreateCompanyDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.CompanyService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("companies")
public class CompanyController {   
    @Autowired
    private CompanyService service;
    
    @PostMapping("/create")
    public Company createCompany(@RequestBody @Valid CreateCompanyDTO data, @AuthenticationPrincipal User user) {
        return service.createCompany(data, user.getId());
    }
}
