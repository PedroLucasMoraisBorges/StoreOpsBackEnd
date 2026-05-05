package com.store_ops_backend.services.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.entities.AccountTransactions;
import com.store_ops_backend.models.entities.CashRegister;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.CompanyExpense;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.TableSession;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.CashRegisterRepository;
import com.store_ops_backend.repositories.CompanyExpenseRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PeopleRepository;
import com.store_ops_backend.repositories.TableSessionRepository;
import com.store_ops_backend.repositories.UserCompanyRepository;

@Service
public class ReportService {
    private static final String CUSTOMER_TYPE = "CLIENT";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AccountTransactionsRepository transactionsRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private CompanyExpenseRepository expenseRepository;

    @Autowired
    private TableSessionRepository tableSessionRepository;

    public byte[] buildOrdersReport(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        Company company = loadCompany(companyId);
        PeriodRange period = PeriodRange.of(dateFrom, dateTo);
        List<Order> orders = orderRepository.findEncomendasByCompanyIdAndScheduledAtBetween(
            companyId,
            period.start(),
            period.end()
        );

        PdfReportBuilder pdf = new PdfReportBuilder(
            "Relatório de Encomendas",
            company.getName(),
            period.label()
        );

        int totalOrders = orders.size();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            BigDecimal orderTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalValue = totalValue.add(orderTotal);

            OffsetDateTime referenceDate = order.getScheduledAt() != null
                ? order.getScheduledAt()
                : order.getCreatedAt();
            String customerName = resolveCustomerName(order);
            String customerShortName = shortName(customerName);
            pdf.addSectionTitle(customerShortName + " • " + formatDateTimeLabel(referenceDate));
            String attendantName = order.getAttendant() != null
                ? order.getAttendant().getUser().getName()
                : "Não informado";

            pdf.addParagraph("Data: " + formatDateTime(order.getCreatedAt()));
            pdf.addParagraph("Cliente: " + customerName);
            pdf.addParagraph("Status: " + order.getStatus());
            pdf.addParagraph("Atendente: " + attendantName);
            pdf.addParagraph("Total: R$ " + formatMoney(orderTotal));

            List<String> headers = List.of("Item", "Qtd", "Unid.", "Vlr Unit.", "Subtotal");
            List<Float> widths = List.of(220f, 60f, 50f, 80f, 80f);
            List<List<String>> rows = new ArrayList<>();
            for (OrderItem item : items) {
                BigDecimal subtotal = item.getUnitPrice().multiply(item.getQuantity());
                rows.add(List.of(
                    item.getName(),
                    item.getQuantity().toPlainString(),
                    item.getUnit(),
                    formatMoney(item.getUnitPrice()),
                    formatMoney(subtotal)
                ));
            }
            pdf.addTable(headers, rows, widths);
        }

        pdf.addSectionTitle("Resumo Geral");
        pdf.addParagraph("Quantidade de encomendas: " + totalOrders);
        pdf.addParagraph("Valor total: R$ " + formatMoney(totalValue));
        pdf.addParagraph("Período: " + period.label());

        return pdf.build();
    }

    public byte[] buildOnlineOrdersReport(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        Company company = loadCompany(companyId);
        PeriodRange period = PeriodRange.of(dateFrom, dateTo);
        List<Order> orders = orderRepository.findOnlineOrdersByCompanyIdAndCreatedAtBetween(
            companyId,
            period.start(),
            period.end()
        );

        PdfReportBuilder pdf = new PdfReportBuilder(
            "Relatório de Pedidos Online",
            company.getName(),
            period.label()
        );

        int totalOrders = orders.size();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            BigDecimal orderTotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalValue = totalValue.add(orderTotal);

            String customerName = resolveCustomerName(order);
            pdf.addSectionTitle(shortName(customerName) + " • " + formatDateTimeLabel(order.getCreatedAt()));

            pdf.addParagraph("Data do pedido: " + formatDateTime(order.getCreatedAt()));
            pdf.addParagraph("Cliente: " + customerName);
            pdf.addParagraph("Tipo: " + ("DELIVERY".equals(order.getType()) ? "Entrega" : "Retirada"));
            if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank()) {
                pdf.addParagraph("Endereço: " + order.getDeliveryAddress());
            }
            pdf.addParagraph("Status: " + order.getStatus());
            pdf.addParagraph("Total: R$ " + formatMoney(orderTotal));

            List<String> headers = List.of("Item", "Qtd", "Unid.", "Vlr Unit.", "Subtotal");
            List<Float> widths = List.of(220f, 60f, 50f, 80f, 80f);
            List<List<String>> rows = new ArrayList<>();
            for (OrderItem item : items) {
                BigDecimal subtotal = item.getUnitPrice().multiply(item.getQuantity());
                rows.add(List.of(
                    item.getName(),
                    item.getQuantity().toPlainString(),
                    item.getUnit(),
                    formatMoney(item.getUnitPrice()),
                    formatMoney(subtotal)
                ));
            }
            pdf.addTable(headers, rows, widths);
        }

        pdf.addSectionTitle("Resumo Geral");
        pdf.addParagraph("Total de pedidos online: " + totalOrders);
        pdf.addParagraph("Valor total: R$ " + formatMoney(totalValue));
        pdf.addParagraph("Período: " + period.label());

        return pdf.build();
    }

    public byte[] buildCashFlowReport(String companyId, LocalDate date) {
        Company company = loadCompany(companyId);
        PeriodRange period = PeriodRange.of(date, date);

        PdfReportBuilder pdf = new PdfReportBuilder(
            "Fluxo de Caixa do Dia",
            company.getName(),
            date.format(DATE_FORMAT)
        );

        // Cash register info
        CashRegister cashRegister = cashRegisterRepository.findOpenByCompanyId(companyId).orElse(null);
        if (cashRegister == null) {
            List<CashRegister> todayRegisters = cashRegisterRepository
                .findByCompanyIdOrderByOpenedAtDesc(companyId)
                .stream()
                .filter(cr -> cr.getOpenedAt().toLocalDate().equals(date))
                .toList();
            cashRegister = todayRegisters.isEmpty() ? null : todayRegisters.get(0);
        }

        pdf.addSectionTitle("Caixa");
        if (cashRegister != null) {
            String opener = cashRegister.getUser().getName() != null
                ? cashRegister.getUser().getName()
                : cashRegister.getUser().getUsername();
            pdf.addParagraph("Turno: " + shiftLabel(cashRegister.getShift()));
            pdf.addParagraph("Responsável: " + opener);
            pdf.addParagraph("Aberto em: " + formatDateTime(cashRegister.getOpenedAt()));
            pdf.addParagraph("Status: " + ("OPEN".equals(cashRegister.getStatus()) ? "Aberto" : "Fechado"));
            if (cashRegister.getClosedAt() != null) {
                pdf.addParagraph("Fechado em: " + formatDateTime(cashRegister.getClosedAt()));
            }
        } else {
            pdf.addParagraph("Nenhum caixa registrado para esta data.");
        }

        // Encomendas concluídas
        List<Order> encomendas = orderRepository.findEncomendasByCompanyIdAndScheduledAtBetween(
            companyId, period.start(), period.end()
        ).stream().filter(o -> "COMPLETED".equals(o.getStatus())).toList();
        BigDecimal encomendasTotal = encomendas.stream()
            .map(o -> orderItemRepository.findByOrderId(o.getId()).stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pedidos online concluídos
        List<Order> onlineOrders = orderRepository.findOnlineOrdersByCompanyIdAndCreatedAtBetween(
            companyId, period.start(), period.end()
        ).stream().filter(o -> "COMPLETED".equals(o.getStatus())).toList();
        BigDecimal onlineOrdersTotal = onlineOrders.stream()
            .map(o -> orderItemRepository.findByOrderId(o.getId()).stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mesas pagas
        List<TableSession> sessions = tableSessionRepository
            .findClosedByCompanyAndClosedAtBetween(companyId, period.start(), period.end())
            .stream().filter(s -> s.getPaidAt() != null).toList();
        BigDecimal sessionsTotal = sessions.stream()
            .map(TableSession::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pagamentos de fiado recebidos
        List<AccountTransactions> payments = transactionsRepository
            .findPaymentsByCompanyAndCreatedAtBetween(companyId, period.start(), period.end());
        BigDecimal paymentsTotal = payments.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = encomendasTotal.add(onlineOrdersTotal).add(sessionsTotal).add(paymentsTotal);

        pdf.addSectionTitle("Entradas");
        List<String> revenueHeaders = List.of("Origem", "Qtd.", "Valor");
        List<Float> revenueWidths = List.of(280f, 80f, 130f);
        List<List<String>> revenueRows = new ArrayList<>();
        revenueRows.add(List.of("Encomendas concluídas", String.valueOf(encomendas.size()), "R$ " + formatMoney(encomendasTotal)));
        revenueRows.add(List.of("Pedidos online concluídos", String.valueOf(onlineOrders.size()), "R$ " + formatMoney(onlineOrdersTotal)));
        revenueRows.add(List.of("Mesas pagas", String.valueOf(sessions.size()), "R$ " + formatMoney(sessionsTotal)));
        revenueRows.add(List.of("Pagamentos de fiado recebidos", String.valueOf(payments.size()), "R$ " + formatMoney(paymentsTotal)));
        pdf.addTable(revenueHeaders, revenueRows, revenueWidths);

        // Despesas
        List<CompanyExpense> expenses = expenseRepository
            .findByCompanyIdAndExpenseDateBetween(companyId, period.start(), period.end());
        BigDecimal expensesTotal = expenses.stream()
            .map(CompanyExpense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Compras fiadas (saídas)
        List<AccountTransactions> fiado = transactionsRepository
            .findFiadoByCompanyAndCreatedAtBetween(companyId, period.start(), period.end());
        BigDecimal fiadoTotal = fiado.stream()
            .map(AccountTransactions::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expensesTotal.add(fiadoTotal);

        pdf.addSectionTitle("Saídas");
        List<String> expenseHeaders = List.of("Origem", "Qtd.", "Valor");
        List<Float> expenseWidths = List.of(280f, 80f, 130f);
        List<List<String>> expenseRows = new ArrayList<>();
        expenseRows.add(List.of("Despesas do estabelecimento", String.valueOf(expenses.size()), "R$ " + formatMoney(expensesTotal)));
        expenseRows.add(List.of("Compras fiadas (crédito concedido)", String.valueOf(fiado.size()), "R$ " + formatMoney(fiadoTotal)));
        pdf.addTable(expenseHeaders, expenseRows, expenseWidths);

        // Balance summary
        BigDecimal balance = totalRevenue.subtract(totalExpenses);
        pdf.addSectionTitle("Resumo do Dia");
        pdf.addParagraph("Total de entradas: R$ " + formatMoney(totalRevenue));
        pdf.addParagraph("Total de saídas: R$ " + formatMoney(totalExpenses));
        pdf.addParagraph("Saldo do dia: R$ " + formatMoney(balance));

        return pdf.build();
    }

    public byte[] buildFiadoReport(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        Company company = loadCompany(companyId);
        PeriodRange period = PeriodRange.of(dateFrom, dateTo);

        List<AccountTransactions> fiadoTransactions = transactionsRepository.findFiadoByCompanyAndCreatedAtBetween(
            companyId,
            period.start(),
            period.end()
        );

        PdfReportBuilder pdf = new PdfReportBuilder(
            "Relatório de Compras Fiadas",
            company.getName(),
            period.label()
        );

        List<String> headers = List.of("Data", "Cliente", "Vendedor", "Valor", "Situação");
        List<Float> widths = List.of(80f, 160f, 140f, 70f, 70f);
        List<List<String>> rows = new ArrayList<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (AccountTransactions transaction : fiadoTransactions) {
            String customerName = transaction.getAccount().getPeople().getName();
            String sellerName = transaction.getUser().getName();
            totalValue = totalValue.add(transaction.getAmount());
            rows.add(List.of(
                formatDateTime(transaction.getCreated_at()),
                customerName,
                sellerName,
                formatMoney(transaction.getAmount()),
                "Aberta"
            ));
        }

        pdf.addTable(headers, rows, widths);
        pdf.addSectionTitle("Resumo Geral");
        pdf.addParagraph("Total de compras fiadas: " + fiadoTransactions.size());
        pdf.addParagraph("Valor total: R$ " + formatMoney(totalValue));
        pdf.addParagraph("Período: " + period.label());

        return pdf.build();
    }

    public byte[] buildCustomersReport(String companyId) {
        Company company = loadCompany(companyId);
        List<People> customers = peopleRepository.findByCompanyIdAndType(companyId);

        PdfReportBuilder pdf = new PdfReportBuilder(
            "Relatório de Clientes",
            company.getName(),
            "Sem período"
        );

        List<String> headers = List.of("Nome", "Contato", "Endereço", "Status");
        List<Float> widths = List.of(170f, 120f, 200f, 60f);
        List<List<String>> rows = new ArrayList<>();

        int active = 0;
        int inactive = 0;

        for (People customer : customers) {
            boolean isActive = Boolean.TRUE.equals(customer.getIs_active());
            if (isActive) {
                active++;
            } else {
                inactive++;
            }
            rows.add(List.of(
                customer.getName(),
                customer.getContact() == null ? "-" : customer.getContact(),
                customer.getAddress() == null ? "-" : customer.getAddress(),
                isActive ? "Ativo" : "Inativo"
            ));
        }

        pdf.addTable(headers, rows, widths);
        pdf.addSectionTitle("Resumo Geral");
        pdf.addParagraph("Total de clientes: " + customers.size());
        pdf.addParagraph("Ativos: " + active);
        pdf.addParagraph("Inativos: " + inactive);

        return pdf.build();
    }

    public byte[] buildEmployeesReport(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        Company company = loadCompany(companyId);
        PeriodRange period = PeriodRange.of(dateFrom, dateTo);

        List<UserCompany> employees = userCompanyRepository.findUsersByCompanyId(companyId);
        PdfReportBuilder pdf = new PdfReportBuilder(
            "Relatório de Funcionários",
            company.getName(),
            period.label()
        );

        int totalOrders = 0;
        BigDecimal totalFiado = BigDecimal.ZERO;

        for (UserCompany employee : employees) {
            String userId = employee.getUser().getId();
            List<Order> orders = orderRepository.findByCompanyIdAndAttendantUserIdAndCreatedAtBetween(
                companyId,
                userId,
                period.start(),
                period.end()
            );
            List<AccountTransactions> fiado = transactionsRepository.findFiadoByCompanyAndUserIdAndCreatedAtBetween(
                companyId,
                userId,
                period.start(),
                period.end()
            );

            BigDecimal employeeFiado = fiado.stream()
                .map(AccountTransactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalOrders += orders.size();
            totalFiado = totalFiado.add(employeeFiado);

            pdf.addSectionTitle(employee.getUser().getName());
            pdf.addParagraph("Login: " + employee.getUser().getLogin());
            pdf.addParagraph("Cargo: " + employee.getRole());
            pdf.addParagraph("Posição: " + (employee.getPosition() == null ? "-" : employee.getPosition()));
            pdf.addParagraph("Status: " + (Boolean.TRUE.equals(employee.getStatus()) ? "Ativo" : "Inativo"));
            pdf.addParagraph("Encomendas atendidas: " + orders.size());
            pdf.addParagraph("Fiado vendido: R$ " + formatMoney(employeeFiado));
        }

        pdf.addSectionTitle("Resumo Geral");
        pdf.addParagraph("Total de funcionários: " + employees.size());
        pdf.addParagraph("Total de encomendas atendidas: " + totalOrders);
        pdf.addParagraph("Total de fiados vendidos: R$ " + formatMoney(totalFiado));

        return pdf.build();
    }

    private String resolveCustomerName(Order order) {
        if (order.getCustomer() != null) {
            return order.getCustomer().getName();
        }
        return order.getCustomerName() != null ? order.getCustomerName() : "Cliente";
    }

    private String shiftLabel(String shift) {
        if (shift == null) return "Não identificado";
        return switch (shift) {
            case "MANHA" -> "Manhã";
            case "TARDE" -> "Tarde";
            case "NOITE" -> "Noite";
            default -> "Outro";
        };
    }

    private Company loadCompany(String companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMAT);
    }

    private String formatDateTimeLabel(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        String day = dateTime.format(DATE_FORMAT);
        String time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return day + " às " + time;
    }

    private String shortName(String name) {
        if (name == null || name.isBlank()) {
            return "Cliente";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        return parts[0] + " " + parts[1];
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }

    public record PeriodRange(OffsetDateTime start, OffsetDateTime end, String label) {
        static PeriodRange of(LocalDate from, LocalDate to) {
            OffsetDateTime start = from.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            OffsetDateTime end = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);
            String label = from.format(DATE_FORMAT) + " até " + to.format(DATE_FORMAT);
            return new PeriodRange(start, end, label);
        }
    }
}
