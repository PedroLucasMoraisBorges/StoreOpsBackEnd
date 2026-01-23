package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CreateEmployeeDTO;
import com.store_ops_backend.models.dtos.EmployeeResponseDTO;
import com.store_ops_backend.models.dtos.TransactionResponseDTO;
import com.store_ops_backend.models.dtos.UpdateEmployeeDTO;
import com.store_ops_backend.services.EmployeeService;

@RestController
@RequestMapping("employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @PostMapping("/create/{companyId}")
    public EmployeeResponseDTO createEmployee(
        @RequestBody CreateEmployeeDTO data,
        @PathVariable("companyId") String companyId
    ) {
        return service.createEmployee(data, companyId);
    }

    @GetMapping("/getAll/{companyId}")
    public List<EmployeeResponseDTO> getAllEmployees(@PathVariable("companyId") String companyId) {
        return service.getAllEmployees(companyId);
    }

    @GetMapping("/get/{companyId}/{userId}")
    public EmployeeResponseDTO getEmployeeById(
        @PathVariable("companyId") String companyId,
        @PathVariable("userId") String userId
    ) {
        return service.getEmployeeById(companyId, userId);
    }

    @PutMapping("/update/{companyId}/{userId}")
    public EmployeeResponseDTO updateEmployee(
        @RequestBody UpdateEmployeeDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("userId") String userId
    ) {
        return service.updateEmployee(companyId, userId, data);
    }

    @DeleteMapping("/delete/{companyId}/{userId}")
    public void deleteEmployee(
        @PathVariable("companyId") String companyId,
        @PathVariable("userId") String userId
    ) {
        service.deleteEmployee(companyId, userId);
    }

    @PutMapping("/alterStatus/{companyId}/{userId}")
    public void updateEmployeeStatus(
        @PathVariable("companyId") String companyId,
        @PathVariable("userId") String userId
    ) {
        service.updateEmployeeStatus(companyId, userId);
    }

    @GetMapping("/transactions/{companyId}/{userId}")
    public List<TransactionResponseDTO> getEmployeeAccountTransactions(
        @PathVariable("companyId") String companyId,
        @PathVariable("userId") String userId
    ) {
        return service.getEmployeeAccountTransactions(companyId, userId);
    }
}
