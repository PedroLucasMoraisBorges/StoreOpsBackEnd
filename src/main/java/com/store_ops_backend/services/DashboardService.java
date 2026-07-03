package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.DashboardDailySummaryDTO;
import com.store_ops_backend.models.dtos.DashboardRecentOrderDTO;
import com.store_ops_backend.models.dtos.DashboardResponseDTO;
import com.store_ops_backend.models.dtos.DashboardStockAlertDTO;
import com.store_ops_backend.models.dtos.DashboardTopCustomerDTO;
import com.store_ops_backend.models.dtos.DashboardTopProductDTO;
import com.store_ops_backend.models.dtos.DashboardWeeklyRevenueDTO;
import com.store_ops_backend.models.dtos.TopProductRawDTO;
import com.store_ops_backend.models.entities.AccountTransactions;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.CompanyExpense;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.Product;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.models.entities.TableSession;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.CompanyExpenseRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PeopleRepository;
import com.store_ops_backend.repositories.ProductRepository;
import com.store_ops_backend.repositories.StockItemRepository;
import com.store_ops_backend.repositories.TableSessionRepository;

@Service
public class DashboardService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountTransactionsRepository transactionsRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private CompanyExpenseRepository expenseRepository;

    @Autowired
    private TableSessionRepository tableSessionRepository;

    @Autowired
    private ProductRepository productRepository;

    public DashboardResponseDTO getDashboard(String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        OffsetDateTime monthStart = firstDayOfMonth.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime monthEnd = lastDayOfMonth.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        OffsetDateTime todayStart = today.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime todayEnd = today.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        int totalCustomers = peopleRepository.findByCompanyIdAndType(companyId).size();

        List<Order> ordersThisMonth = orderRepository.findByCompanyIdAndScheduledAtBetween(companyId, monthStart, monthEnd);
        Map<String, BigDecimal> monthTotals = batchOrderTotals(ordersThisMonth);
        int monthOrdersCount = ordersThisMonth.size();
        BigDecimal monthRevenue = sumCompletedOrders(ordersThisMonth, monthTotals);

        double revenueChangePercent = buildRevenueChangePercent(companyId, firstDayOfMonth, monthRevenue, zone);

        List<DashboardWeeklyRevenueDTO> weeklyRevenue = buildWeeklyRevenue(companyId, zone);

        DashboardDailySummaryDTO dailySummary = buildDailySummary(companyId, todayStart, todayEnd);

        List<DashboardRecentOrderDTO> recentOrders = buildRecentOrders(companyId);
        List<DashboardTopCustomerDTO> topCustomers = buildTopCustomers(companyId);

        List<StockItem> alertItems = stockItemRepository.findBelowMinimum(companyId);
        int stockAlertsCount = alertItems.size();
        List<DashboardStockAlertDTO> stockAlerts = alertItems.stream()
            .limit(5)
            .map(s -> new DashboardStockAlertDTO(
                s.getProduct().getId(),
                s.getProduct().getName(),
                s.getProduct().getUnit(),
                s.getQuantity(),
                s.getMinQuantity()
            ))
            .toList();

        List<TopProductRawDTO> rawProducts = orderItemRepository.findTopProductsByCompanyId(
            companyId, monthStart, monthEnd, PageRequest.of(0, 10)
        );
        List<DashboardTopProductDTO> topProducts = enrichWithMenuEngineering(rawProducts, companyId);

        return new DashboardResponseDTO(
            totalCustomers,
            monthOrdersCount,
            scale(monthRevenue),
            revenueChangePercent,
            weeklyRevenue,
            dailySummary,
            recentOrders,
            topCustomers,
            stockAlertsCount,
            stockAlerts,
            topProducts
        );
    }

    private List<DashboardTopProductDTO> enrichWithMenuEngineering(List<TopProductRawDTO> raw, String companyId) {
        if (raw.isEmpty()) return List.of();

        // Build name → costPrice map from catalog
        Map<String, BigDecimal> costByName = productRepository.findByCompanyIdAndActiveOrderByNameAsc(companyId, true)
            .stream()
            .filter(p -> p.getCostPrice() != null && p.getSellPrice() != null
                         && p.getSellPrice().compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toMap(
                Product::getName,
                p -> p.getCostPrice().divide(p.getSellPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)),
                (a, b) -> a  // keep first if duplicate names
            ));

        // Compute marginPercent for each product
        List<BigDecimal> quantities  = new ArrayList<>();
        List<BigDecimal> margins     = new ArrayList<>();

        List<BigDecimal[]> enriched = new ArrayList<>();
        for (TopProductRawDTO r : raw) {
            BigDecimal costPct = costByName.getOrDefault(r.name(), null);
            BigDecimal margin  = costPct != null
                ? BigDecimal.valueOf(100).subtract(costPct).setScale(1, RoundingMode.HALF_UP)
                : null;
            enriched.add(new BigDecimal[]{ r.totalQuantity(), margin });
            quantities.add(r.totalQuantity());
            if (margin != null) margins.add(margin);
        }

        BigDecimal medianQty    = median(quantities);
        BigDecimal medianMargin = median(margins);

        List<DashboardTopProductDTO> result = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            TopProductRawDTO r     = raw.get(i);
            BigDecimal qty         = enriched.get(i)[0];
            BigDecimal margin      = enriched.get(i)[1];

            String classification = null;
            if (margin != null && medianMargin != null) {
                boolean highQty    = qty.compareTo(medianQty)    >= 0;
                boolean highMargin = margin.compareTo(medianMargin) >= 0;
                classification = highQty && highMargin  ? "STAR"
                               : highQty                ? "PLOW_HORSE"
                               : highMargin             ? "PUZZLE"
                               :                          "DOG";
            }

            result.add(new DashboardTopProductDTO(
                r.name(),
                r.totalQuantity(),
                r.totalRevenue(),
                margin,
                classification
            ));
        }
        return result;
    }

    private BigDecimal median(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        List<BigDecimal> sorted = values.stream().sorted().toList();
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return sorted.get(mid - 1).add(sorted.get(mid))
                         .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        }
        return sorted.get(mid);
    }

    private double buildRevenueChangePercent(String companyId, LocalDate firstDayOfMonth,
                                              BigDecimal currentRevenue, ZoneId zone) {
        LocalDate firstDayOfPrevMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfPrevMonth = firstDayOfMonth.minusDays(1);
        OffsetDateTime prevStart = firstDayOfPrevMonth.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime prevEnd = lastDayOfPrevMonth.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        List<Order> prevOrders = orderRepository.findByCompanyIdAndScheduledAtBetween(companyId, prevStart, prevEnd);
        Map<String, BigDecimal> prevTotals = batchOrderTotals(prevOrders);
        BigDecimal prevRevenue = sumCompletedOrders(prevOrders, prevTotals);

        if (prevRevenue.compareTo(BigDecimal.ZERO) > 0) {
            return currentRevenue.subtract(prevRevenue)
                .divide(prevRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        return currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
    }

    private List<DashboardWeeklyRevenueDTO> buildWeeklyRevenue(String companyId, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        OffsetDateTime start = weekStart.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = weekEnd.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        List<Order> completedOrders = orderRepository.findCompletedByCompanyIdAndCreatedAtBetween(companyId, start, end);
        Map<String, BigDecimal> totals = batchOrderTotals(completedOrders);
        Map<LocalDate, BigDecimal> totalsByDay = new HashMap<>();

        for (Order order : completedOrders) {
            LocalDate day = order.getScheduledAt().atZoneSameInstant(zone).toLocalDate();
            BigDecimal total = totals.getOrDefault(order.getId(), BigDecimal.ZERO);
            totalsByDay.merge(day, total, BigDecimal::add);
        }

        List<DashboardWeeklyRevenueDTO> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            BigDecimal total = totalsByDay.getOrDefault(day, BigDecimal.ZERO);
            result.add(new DashboardWeeklyRevenueDTO(dayLabel(day.getDayOfWeek()), scale(total)));
        }
        return result;
    }

    private DashboardDailySummaryDTO buildDailySummary(String companyId, OffsetDateTime from, OffsetDateTime to) {
        List<AccountTransactions> payments = transactionsRepository.findPaymentsByCompanyAndCreatedAtBetween(companyId, from, to);
        List<AccountTransactions> fiado = transactionsRepository.findFiadoByCompanyAndCreatedAtBetween(companyId, from, to);
        List<Order> completedOrders = orderRepository.findCompletedByCompanyIdAndCreatedAtBetween(companyId, from, to);
        List<CompanyExpense> todayExpenses = expenseRepository.findByCompanyIdAndExpenseDateBetween(companyId, from, to);
        List<TableSession> closedSessions = tableSessionRepository.findClosedByCompanyAndClosedAtBetween(companyId, from, to);

        Map<String, BigDecimal> orderTotalsMap = batchOrderTotals(completedOrders);

        BigDecimal paymentsTotal = payments.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal completedOrdersTotal = completedOrders.stream()
            .map(o -> orderTotalsMap.getOrDefault(o.getId(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sessionsTodayTotal = closedSessions.stream()
            .filter(s -> s.getPaidAt() != null)
            .map(TableSession::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expensesTotal = todayExpenses.stream()
            .map(CompanyExpense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal entriesTotal = paymentsTotal.add(completedOrdersTotal).add(sessionsTodayTotal);
        BigDecimal fiadoTotal = fiado.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outputsTotal = fiadoTotal.add(expensesTotal);

        int entriesCount = payments.size() + completedOrders.size() + (int) closedSessions.stream().filter(s -> s.getPaidAt() != null).count();
        int outputsCount = fiado.size() + todayExpenses.size();

        BigDecimal balance = entriesTotal.subtract(outputsTotal);

        return new DashboardDailySummaryDTO(
            entriesCount,
            scale(entriesTotal),
            outputsCount,
            scale(outputsTotal),
            scale(balance)
        );
    }

    private List<DashboardRecentOrderDTO> buildRecentOrders(String companyId) {
        List<Order> orders = orderRepository.findRecentByCompanyId(companyId, PageRequest.of(0, 5));
        if (orders.isEmpty()) return List.of();
        Map<String, BigDecimal> totals = batchOrderTotals(orders);
        return orders.stream()
            .map(order -> {
                String name = order.getCustomer() != null
                    ? order.getCustomer().getName()
                    : order.getCustomerName();
                return new DashboardRecentOrderDTO(
                    order.getId(),
                    name,
                    order.getStatus(),
                    scale(totals.getOrDefault(order.getId(), BigDecimal.ZERO)),
                    order.getCreatedAt(),
                    order.getScheduledAt()
                );
            })
            .toList();
    }

    private List<DashboardTopCustomerDTO> buildTopCustomers(String companyId) {
        return transactionsRepository.findTopCustomersByBalance(companyId, PageRequest.of(0, 5))
            .stream()
            .map(dto -> new DashboardTopCustomerDTO(dto.id(), dto.name(), scale(dto.balance())))
            .toList();
    }

    private BigDecimal sumCompletedOrders(List<Order> orders, Map<String, BigDecimal> totals) {
        return orders.stream()
            .filter(order -> "COMPLETED".equals(order.getStatus()))
            .map(order -> totals.getOrDefault(order.getId(), BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> batchOrderTotals(List<Order> orders) {
        if (orders.isEmpty()) return Map.of();
        List<String> ids = orders.stream().map(Order::getId).toList();
        return orderItemRepository.findByOrderIdIn(ids).stream()
            .collect(Collectors.groupingBy(
                i -> i.getOrder().getId(),
                Collectors.reducing(BigDecimal.ZERO,
                    i -> i.getUnitPrice().multiply(i.getQuantity()),
                    BigDecimal::add)
            ));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String dayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Seg";
            case TUESDAY -> "Ter";
            case WEDNESDAY -> "Qua";
            case THURSDAY -> "Qui";
            case FRIDAY -> "Sex";
            case SATURDAY -> "Sáb";
            case SUNDAY -> "Dom";
        };
    }
}
