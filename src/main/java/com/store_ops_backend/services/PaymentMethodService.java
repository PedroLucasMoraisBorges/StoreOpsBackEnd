package com.store_ops_backend.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.PaymentMethodDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.CompanyPaymentMethods;
import com.store_ops_backend.models.entities.PaymentMethods;
import com.store_ops_backend.repositories.CompanyPaymentMethodsRepository;
import com.store_ops_backend.repositories.PaymentMethodRepository;

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

}
