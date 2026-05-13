package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.CreateExpenseDTO;
import com.store_ops_backend.models.dtos.ExpenseResponseDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @PostMapping("/create/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponseDTO create(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid CreateExpenseDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return expenseService.create(companyId, data);
    }

    @GetMapping("/getAll/{companyId}")
    public List<ExpenseResponseDTO> getAll(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return expenseService.listAll(companyId);
    }

    @DeleteMapping("/delete/{companyId}/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable("companyId") String companyId,
        @PathVariable("expenseId") String expenseId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        expenseService.delete(companyId, expenseId);
    }
}
