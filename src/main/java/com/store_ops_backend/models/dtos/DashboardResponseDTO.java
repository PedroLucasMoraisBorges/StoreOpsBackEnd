package com.store_ops_backend.models.dtos;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDTO(
    int totalCustomers,
    int monthOrdersCount,
    BigDecimal monthRevenue,
    List<DashboardWeeklyRevenueDTO> weeklyRevenue,
    DashboardDailySummaryDTO dailySummary,
    List<DashboardRecentOrderDTO> recentOrders,
    List<DashboardTopCustomerDTO> topCustomers
) {}
