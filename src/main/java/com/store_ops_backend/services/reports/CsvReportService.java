package com.store_ops_backend.services.reports;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.ProductProfitabilityDTO;
import com.store_ops_backend.models.entities.AccountTransactions;
import com.store_ops_backend.models.entities.CompanyExpense;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.models.entities.TableSession;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.CompanyExpenseRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.StockItemRepository;
import com.store_ops_backend.repositories.TableSessionRepository;
import com.store_ops_backend.services.ProfitabilityService;
import com.store_ops_backend.services.reports.ReportService.PeriodRange;

@Service
public class CsvReportService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private AccountTransactionsRepository transactionsRepository;
    @Autowired private CompanyExpenseRepository expenseRepository;
    @Autowired private TableSessionRepository tableSessionRepository;
    @Autowired private StockItemRepository stockItemRepository;
    @Autowired private ProfitabilityService profitabilityService;

    public byte[] buildOrdersCsv(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        PeriodRange period = PeriodRange.of(dateFrom, dateTo);
        List<Order> orders = orderRepository.findEncomendasByCompanyIdAndScheduledAtBetween(
            companyId, period.start(), period.end());

        String[] headers = {"Data", "Data Entrega", "Cliente", "Tipo", "Status", "Atendente", "Total (R$)"};
        List<Object[]> rows = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            String customerName = order.getCustomer() != null
                ? order.getCustomer().getName()
                : (order.getCustomerName() != null ? order.getCustomerName() : "Cliente");
            String attendant = order.getAttendant() != null
                ? order.getAttendant().getUser().getName()
                : "N/A";

            rows.add(new Object[]{
                formatDateTime(order.getCreatedAt()),
                order.getScheduledAt() != null ? formatDateTime(order.getScheduledAt()) : "",
                customerName,
                order.getType(),
                order.getStatus(),
                attendant,
                total.toPlainString()
            });
        }

        return buildCsv(headers, rows);
    }

    public byte[] buildCashFlowCsv(String companyId, LocalDate date) {
        PeriodRange period = PeriodRange.of(date, date);

        List<AccountTransactions> fiado = transactionsRepository
            .findFiadoByCompanyAndCreatedAtBetween(companyId, period.start(), period.end());
        List<AccountTransactions> payments = transactionsRepository
            .findPaymentsByCompanyAndCreatedAtBetween(companyId, period.start(), period.end());
        List<CompanyExpense> expenses = expenseRepository
            .findByCompanyIdAndExpenseDateBetween(companyId, period.start(), period.end());
        List<TableSession> sessions = tableSessionRepository
            .findClosedByCompanyAndClosedAtBetween(companyId, period.start(), period.end())
            .stream().filter(s -> s.getPaidAt() != null).toList();
        List<Order> paidOrders = orderRepository
            .findEncomendasByCompanyIdAndScheduledAtBetween(companyId, period.start(), period.end())
            .stream().filter(o -> o.getPaidAt() != null).toList();

        String[] headers = {"Data", "Descrição", "Tipo", "Categoria", "Valor (R$)", "Direção"};
        List<Object[]> rows = new ArrayList<>();

        for (AccountTransactions t : fiado) {
            rows.add(new Object[]{
                formatDateTime(t.getCreated_at()),
                t.getAccount().getPeople().getName(),
                "FIADO",
                "Compra Fiada",
                t.getAmount().toPlainString(),
                "Saída"
            });
        }
        for (AccountTransactions t : payments) {
            rows.add(new Object[]{
                formatDateTime(t.getCreated_at()),
                t.getAccount().getPeople().getName(),
                "PAGAMENTO_FIADO",
                "Recebimento",
                t.getAmount().toPlainString(),
                "Entrada"
            });
        }
        for (CompanyExpense e : expenses) {
            rows.add(new Object[]{
                formatDateTime(e.getExpenseDate()),
                e.getDescription() != null ? e.getDescription() : "",
                "DESPESA",
                e.getCategory() != null ? e.getCategory() : "",
                e.getAmount().toPlainString(),
                "Saída"
            });
        }
        for (TableSession s : sessions) {
            rows.add(new Object[]{
                formatDateTime(s.getPaidAt()),
                "Mesa fechada",
                "MESA",
                "Atendimento",
                s.getTotal() != null ? s.getTotal().toPlainString() : "0",
                "Entrada"
            });
        }
        for (Order o : paidOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
            BigDecimal total = items.stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            String customer = o.getCustomer() != null ? o.getCustomer().getName()
                : (o.getCustomerName() != null ? o.getCustomerName() : "Cliente");
            rows.add(new Object[]{
                formatDateTime(o.getPaidAt()),
                "Encomenda - " + customer,
                "ENCOMENDA",
                "Venda",
                total.toPlainString(),
                "Entrada"
            });
        }

        return buildCsv(headers, rows);
    }

    public byte[] buildProfitabilityCsv(String companyId, LocalDate dateFrom, LocalDate dateTo) {
        List<ProductProfitabilityDTO> products = profitabilityService.getProductProfitability(companyId, dateFrom, dateTo);

        String[] headers = {"Produto", "Categoria", "Qtd Vendida", "Receita (R$)", "Custo Total (R$)", "Margem Bruta (R$)", "Margem %"};
        List<Object[]> rows = new ArrayList<>();

        for (ProductProfitabilityDTO p : products) {
            rows.add(new Object[]{
                p.productName(),
                p.category() != null ? p.category() : "",
                p.totalQuantitySold().toPlainString(),
                p.totalRevenue().toPlainString(),
                p.totalCost().toPlainString(),
                p.grossMargin().toPlainString(),
                p.marginPercent().toPlainString()
            });
        }

        return buildCsv(headers, rows);
    }

    public byte[] buildStockCsv(String companyId) {
        List<StockItem> items = stockItemRepository.findByCompanyIdOrderByProductNameAsc(companyId);

        String[] headers = {"Produto", "Categoria", "Variante", "Componente", "Quantidade", "Unidade", "Mínimo", "Alerta"};
        List<Object[]> rows = new ArrayList<>();

        for (StockItem item : items) {
            boolean belowMin = item.getMinQuantity() != null
                && item.getQuantity().compareTo(item.getMinQuantity()) < 0;

            rows.add(new Object[]{
                item.getProduct().getName(),
                item.getProduct().getCategory() != null ? item.getProduct().getCategory() : "",
                item.getVariant() != null ? item.getVariant().getName() : "",
                item.getComponentOption() != null ? item.getComponentOption().getName() : "",
                item.getQuantity().toPlainString(),
                item.getProduct().getUnit() != null ? item.getProduct().getUnit() : "",
                item.getMinQuantity() != null ? item.getMinQuantity().toPlainString() : "0",
                belowMin ? "Sim" : "Não"
            });
        }

        return buildCsv(headers, rows);
    }

    private byte[] buildCsv(String[] headers, List<Object[]> rows) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(UTF8_BOM);
            try (CSVPrinter printer = new CSVPrinter(
                    new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
                for (Object[] row : rows) {
                    printer.printRecord(row);
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }
    }

    private String formatDateTime(OffsetDateTime dt) {
        if (dt == null) return "";
        return dt.format(DATE_TIME_FMT);
    }
}
