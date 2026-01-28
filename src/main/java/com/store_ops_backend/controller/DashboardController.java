package com.store_ops_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.DashboardResponseDTO;
import com.store_ops_backend.services.DashboardService;

@RestController
@RequestMapping("dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponseDTO> getSummary(@RequestParam("companyId") String companyId) {
        return ResponseEntity.ok(dashboardService.getDashboard(companyId));
    }
}
