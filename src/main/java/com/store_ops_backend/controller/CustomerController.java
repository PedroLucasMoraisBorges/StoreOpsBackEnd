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
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CreateCustomerDTO;
import com.store_ops_backend.models.dtos.CreateTransactionDTO;
import com.store_ops_backend.models.dtos.CustomerResponseDTO;
import com.store_ops_backend.models.dtos.TransactionResponseDTO;
import com.store_ops_backend.models.dtos.UpdateCustomerDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.AccountTransactionService;
import com.store_ops_backend.services.CustomerService;

@RestController
@RequestMapping("customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @PostMapping("/create/{companyId}")
    public CustomerResponseDTO createCustomer(
        @RequestBody CreateCustomerDTO data,
        @PathVariable("companyId") String companyId
    ) {
        return customerService.createCustomer(data, companyId);
    }

    @GetMapping("/getAll/{companyId}")
    public List<CustomerResponseDTO> getAllCustomers(@PathVariable("companyId") String companyId) {
        return customerService.getAllCustomers(companyId);
    }

    @GetMapping("/get/{companyId}/{customerId}")
    public CustomerResponseDTO getCustomerById(
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId
    ) {
        return customerService.getCustomerById(companyId, customerId);
    }

    @PutMapping("/update/{companyId}/{customerId}")
    public CustomerResponseDTO updateCustomer(
        @RequestBody UpdateCustomerDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId
    ) {
        return customerService.updateCustomer(companyId, customerId, data);
    }

    @PostMapping("/debit/{companyId}/{customerId}")
    public TransactionResponseDTO createDebit(
        @RequestBody CreateTransactionDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId,
        @AuthenticationPrincipal User user
    ) {
        return accountTransactionService.createDebit(companyId, customerId, data, user);
    }

    @PostMapping("/payment/{companyId}/{customerId}")
    public TransactionResponseDTO createPayment(
        @RequestBody CreateTransactionDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId,
        @AuthenticationPrincipal User user
    ) {
        
        return accountTransactionService.createPayment(companyId, customerId, data, user);
    }

    @GetMapping("/transactions/{companyId}/{customerId}")
    public List<TransactionResponseDTO> getTransactions(
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId
    ) {
        return accountTransactionService.listTransactions(companyId, customerId);
    }
}
