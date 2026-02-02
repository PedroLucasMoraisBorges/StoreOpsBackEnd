package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.store_ops_backend.models.dtos.DashboardTopCustomerDTO;
import com.store_ops_backend.models.dtos.DashboardWeeklyRevenueDTO;
import com.store_ops_backend.models.entities.AccountTransactions;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PeopleRepository;

@Service
public class DashboardService {
    private static final String CUSTOMER_TYPE = "CLIENT";

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
        int monthOrdersCount = ordersThisMonth.size();
        BigDecimal monthRevenue = sumCompletedOrders(ordersThisMonth);

        List<DashboardWeeklyRevenueDTO> weeklyRevenue = buildWeeklyRevenue(companyId, zone);

        DashboardDailySummaryDTO dailySummary = buildDailySummary(companyId, todayStart, todayEnd);

        List<DashboardRecentOrderDTO> recentOrders = buildRecentOrders(companyId);
        List<DashboardTopCustomerDTO> topCustomers = buildTopCustomers(companyId);

        return new DashboardResponseDTO(
            totalCustomers,
            monthOrdersCount,
            scale(monthRevenue),
            weeklyRevenue,
            dailySummary,
            recentOrders,
            topCustomers
        );
    }

    private List<DashboardWeeklyRevenueDTO> buildWeeklyRevenue(String companyId, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        OffsetDateTime start = weekStart.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = weekEnd.plusDays(1).atStartOfDay(zone).toOffsetDateTime().minusNanos(1);

        List<Order> completedOrders = orderRepository.findCompletedByCompanyIdAndCreatedAtBetween(companyId, start, end);
        Map<LocalDate, BigDecimal> totalsByDay = new HashMap<>();

        for (Order order : completedOrders) {
            LocalDate day = order.getScheduledAt().atZoneSameInstant(zone).toLocalDate();
            BigDecimal total = orderTotal(order);
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

        BigDecimal paymentsTotal = payments.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal completedOrdersTotal = completedOrders.stream()
            .map(this::orderTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal entriesTotal = paymentsTotal.add(completedOrdersTotal);
        BigDecimal outputsTotal = fiado.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int entriesCount = payments.size() + completedOrders.size();
        int outputsCount = fiado.size();

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
        List<Order> orders = orderRepository.findByCompanyId(companyId);
        return orders.stream()
            .limit(5)
            .map(order -> new DashboardRecentOrderDTO(
                order.getId(),
                order.getCustomer().getName(),
                order.getStatus(),
                scale(orderTotal(order)),
                order.getCreatedAt(),
                order.getScheduledAt()
            ))
            .toList();
    }

    private List<DashboardTopCustomerDTO> buildTopCustomers(String companyId) {
        List<AccountTransactions> allTransactions = transactionsRepository.findByCompanyId(companyId);
        Map<String, BigDecimal> balances = new HashMap<>();
        Map<String, People> peopleById = peopleRepository.findByCompanyIdAndType(companyId)
            .stream()
            .collect(Collectors.toMap(People::getId, p -> p));

        for (AccountTransactions transaction : allTransactions) {
            People customer = transaction.getAccount().getPeople();
            if (customer == null || !peopleById.containsKey(customer.getId())) {
                continue;
            }
            BigDecimal value = transaction.getAmount();
            if ("CUSTOMER_PAYMENT".equals(transaction.getOrigin())) {
                value = value.negate();
            }
            balances.merge(customer.getId(), value, BigDecimal::add);
        }

        return balances.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(5)
            .map(entry -> {
                People person = peopleById.get(entry.getKey());
                return new DashboardTopCustomerDTO(
                    entry.getKey(),
                    person == null ? "-" : person.getName(),
                    scale(entry.getValue())
                );
            })
            .toList();
    }

    private BigDecimal sumCompletedOrders(List<Order> orders) {
        return orders.stream()
            .filter(order -> "COMPLETED".equals(order.getStatus()))
            .map(this::orderTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal orderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return items.stream()
            .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
