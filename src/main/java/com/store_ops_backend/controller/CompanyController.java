package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CompanyResponseDTO;
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
    public CompanyResponseDTO createCompany(@RequestBody @Valid CreateCompanyDTO data, @AuthenticationPrincipal User user) {
        return service.createCompany(data, user.getId());
    }

    @PutMapping("/update")
    public Company updateCompany(@RequestBody @Valid CreateCompanyDTO data, @AuthenticationPrincipal User user) {
        // return service.updateCompany(data, user.getId());
        return null;
    }

    @GetMapping("/getCompany/{id}")
    public Company getCompany(@PathVariable("id") String id) {
        // return service.getCompanyById(id);
        return null;
    }

    @GetMapping("/getCompanys")
    public List<Company> getCompanies(@RequestParam(value = "filter", defaultValue = "nenhum") String filter) {
        // return service.getAllCompanies();
        return null;
    }
}
