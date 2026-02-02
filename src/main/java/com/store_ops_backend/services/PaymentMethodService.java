package com.store_ops_backend.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.PaymentMethodDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.CompanyPaymentMethods;
import com.store_ops_backend.models.entities.PaymentMethods;
import com.store_ops_backend.repositories.CompanyPaymentMethodsRepository;
import com.store_ops_backend.repositories.PaymentMethodRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentMethodService {
    @Autowired
    PaymentMethodRepository repository;

    @Autowired
    CompanyPaymentMethodsRepository companyPaymentMethodsRepository;

    public void createCompanyPaymentMethods(Company company, List<String> paymentMethodIds) {
        paymentMethodIds.forEach(paymentMethodId -> {
            PaymentMethods paymentMethods = repository.findById(paymentMethodId).orElseThrow();

            CompanyPaymentMethods companyPaymentMethods = new CompanyPaymentMethods(company, paymentMethods);


            companyPaymentMethodsRepository.save(companyPaymentMethods);
        });
    }

    public List<PaymentMethodDTO> getCompanyPaymentMethods(String companyId) {
        List<PaymentMethodDTO> paymentMethodDTOs = new ArrayList<PaymentMethodDTO>();
        List<CompanyPaymentMethods> companyPaymentMethods = companyPaymentMethodsRepository.findByCompanyId(companyId);

        for(CompanyPaymentMethods cpm : companyPaymentMethods) {
            PaymentMethods paymentMethod = repository.findById(cpm.getPaymentMethod().getId()).orElseThrow();
            paymentMethodDTOs.add(
                new PaymentMethodDTO(
                    paymentMethod.getId(),
                    paymentMethod.getName(),
                    paymentMethod.getCode()
                )
            );
        }

        return paymentMethodDTOs;
    }   

    public List<PaymentMethodDTO> getAllPaymentMethods() {
        return repository.findAll()
            .stream()
            .map(method -> new PaymentMethodDTO(
                method.getId(),
                method.getName(),
                method.getCode()
            ))
            .toList();
    }

    @Transactional
    public void syncCompanyPaymentMethods(
            Company company,
            List<String> newPaymentMethodIds
    ) {
        var current = companyPaymentMethodsRepository
            .findByCompanyId(company.getId());

        var currentIds = current.stream()
            .map(cpm -> cpm.getPaymentMethod().getId())
            .collect(Collectors.toSet());

        var incomingIds = new HashSet<>(newPaymentMethodIds);

        current.stream()
            .filter(cpm -> !incomingIds.contains(cpm.getPaymentMethod().getId()))
            .forEach(companyPaymentMethodsRepository::delete);

        incomingIds.stream()
            .filter(id -> !currentIds.contains(id))
            .forEach(id -> {
                PaymentMethods pm = repository.findById(id).orElseThrow();
                companyPaymentMethodsRepository.save(
                    new CompanyPaymentMethods(company, pm)
                );
            });
    }


}
