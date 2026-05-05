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

import com.store_ops_backend.models.dtos.CashRegisterResponseDTO;
import com.store_ops_backend.models.dtos.OpenCashRegisterDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.CashRegisterService;

@RestController
@RequestMapping("cash-registers")
public class CashRegisterController {

    @Autowired
    private CashRegisterService cashRegisterService;

    @PostMapping("/open/{companyId}")
    public CashRegisterResponseDTO open(
        @PathVariable("companyId") String companyId,
        @RequestBody(required = false) OpenCashRegisterDTO data,
        @AuthenticationPrincipal User user
    ) {
        return cashRegisterService.open(companyId, user, data != null ? data : new OpenCashRegisterDTO(null));
    }

    @PutMapping("/close/{companyId}/{registerId}")
    public CashRegisterResponseDTO close(
        @PathVariable("companyId") String companyId,
        @PathVariable("registerId") String registerId
    ) {
        return cashRegisterService.close(companyId, registerId);
    }

    @PutMapping("/reopen/{companyId}/{registerId}")
    public CashRegisterResponseDTO reopen(
        @PathVariable("companyId") String companyId,
        @PathVariable("registerId") String registerId,
        @AuthenticationPrincipal User user
    ) {
        return cashRegisterService.reopen(companyId, registerId, user.getId());
    }

    @GetMapping("/all/{companyId}")
    public List<CashRegisterResponseDTO> getAll(@PathVariable("companyId") String companyId) {
        return cashRegisterService.getAll(companyId);
    }

    @GetMapping("/current/{companyId}")
    public CashRegisterResponseDTO getCurrent(@PathVariable("companyId") String companyId) {
        return cashRegisterService.getCurrent(companyId);
    }
}
