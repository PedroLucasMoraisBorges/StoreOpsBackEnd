package com.store_ops_backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CreateStockMovementDTO;
import com.store_ops_backend.models.dtos.StockItemResponseDTO;
import com.store_ops_backend.models.dtos.StockMovementResponseDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.StockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("stock")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/getAll/{companyId}")
    public List<StockItemResponseDTO> getAll(@PathVariable("companyId") String companyId) {
        return stockService.listStock(companyId);
    }

    @GetMapping("/alerts/{companyId}")
    public List<StockItemResponseDTO> getAlerts(@PathVariable("companyId") String companyId) {
        return stockService.listBelowMinimum(companyId);
    }

    @GetMapping("/movements/{companyId}")
    public List<StockMovementResponseDTO> getMovements(
        @PathVariable("companyId") String companyId,
        @RequestParam(value = "productId", required = false) String productId
    ) {
        return productId != null
            ? stockService.listMovementsByProduct(companyId, productId)
            : stockService.listMovements(companyId);
    }

    @PostMapping("/move/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponseDTO registerMovement(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid CreateStockMovementDTO data,
        @AuthenticationPrincipal User user
    ) {
        return stockService.registerMovement(companyId, data, user);
    }

    @PutMapping("/min/{companyId}/{productId}")
    public StockItemResponseDTO setMinQuantity(
        @PathVariable("companyId") String companyId,
        @PathVariable("productId") String productId,
        @RequestParam("value") BigDecimal value,
        @RequestParam(value = "variantId", required = false) String variantId,
        @RequestParam(value = "componentOptionId", required = false) String componentOptionId
    ) {
        return stockService.setMinQuantity(companyId, productId, value, variantId, componentOptionId);
    }
}
