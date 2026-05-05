package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.store_ops_backend.models.dtos.CompanyResponseDTO;
import com.store_ops_backend.models.dtos.CreateCompanyDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.services.CompanyService;

import jakarta.persistence.EntityNotFoundException;

import jakarta.validation.Valid;


@RestController
@RequestMapping("companies")
public class CompanyController {

    @Autowired
    private CompanyService service;

    @Autowired
    private CompanyRepository companyRepository;
    
    @PostMapping("/create")
    public CompanyResponseDTO createCompany(@RequestBody @Valid CreateCompanyDTO data, @AuthenticationPrincipal User user) {
        return service.createCompany(data, user.getId());
    }

    @PutMapping("/update/{id}")
    public CompanyResponseDTO updateCompany(@RequestBody @Valid CreateCompanyDTO data, @AuthenticationPrincipal User user, @PathVariable("id") String id) {
        return service.updateCompany(id, data);
    }

    @GetMapping("/getCompany/{id}")
    public CompanyResponseDTO getCompany(@PathVariable("id") String id) {
        return service.getCompanyById(id);
    }

    @GetMapping("/getUserCompanies")
    public List<CompanyResponseDTO> getCompanies(@RequestParam(value = "filter", defaultValue = "nenhum") String filter, @AuthenticationPrincipal User user) {
        return service.getAllUserCompanies(user.getId(), filter);
    }

    @PutMapping("/slug/{id}")
    public CompanyResponseDTO updateSlug(
        @PathVariable("id") String id,
        @RequestParam("slug") String slug
    ) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        String normalized = slug.toLowerCase().replaceAll("[^a-z0-9-]", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Slug inválido");
        }

        company.updateSlug(normalized);
        companyRepository.save(company);
        return service.getCompanyById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") String id, @AuthenticationPrincipal User user) {
        service.deleteCompany(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
