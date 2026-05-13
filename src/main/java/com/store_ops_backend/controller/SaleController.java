package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.CreateCounterSaleDTO;
import com.store_ops_backend.models.dtos.OrderResponseDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.SaleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @PostMapping("/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO createCounterSale(
        @RequestBody @Valid CreateCounterSaleDTO data,
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return saleService.createCounterSale(data, companyId);
    }
}
