package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.services.EmployeeService;

@RestController
@RequestMapping("employees")
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @PostMapping("/create")
    public void createEmployee() {
        // Implementation for creating an employee will go here
    }

    @GetMapping("/getAll/{id}")
    public void getAllEmployees(@PathVariable("id") String id) {
        // Implementation for getting all employees will go here
    }
}
