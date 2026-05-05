package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
    int totalCustomers,
    int monthOrdersCount,
    BigDecimal monthRevenue,
    double revenueChangePercent,
    List<DashboardWeeklyRevenueDTO> weeklyRevenue,
    DashboardDailySummaryDTO dailySummary,
    List<DashboardRecentOrderDTO> recentOrders,
    List<DashboardTopCustomerDTO> topCustomers,
    int stockAlertsCount,
    List<DashboardStockAlertDTO> stockAlerts
) {}
