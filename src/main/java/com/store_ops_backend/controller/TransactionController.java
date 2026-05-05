package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CompanyTransactionResponseDTO;
import com.store_ops_backend.services.AccountTransactionService;

@RestController
@RequestMapping("transactions")
public class TransactionController {

    @Autowired
    private AccountTransactionService accountTransactionService;

    @GetMapping("/getAll/{companyId}")
    public List<CompanyTransactionResponseDTO> getAllByCompany(
        @PathVariable("companyId") String companyId
    ) {
        return accountTransactionService.listByCompany(companyId);
    }
}
