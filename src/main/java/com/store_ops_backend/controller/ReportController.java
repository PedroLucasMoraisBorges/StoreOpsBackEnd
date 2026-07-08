package com.store_ops_backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.ProductProfitabilityDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.ProfitabilityService;
import com.store_ops_backend.services.reports.CsvReportService;
import com.store_ops_backend.services.reports.ReportService;

@RestController
@RequestMapping("reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CsvReportService csvReportService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @Autowired
    private ProfitabilityService profitabilityService;

    @GetMapping("/orders")
    public ResponseEntity<byte[]> ordersReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildOrdersReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-encomendas", dateFrom, dateTo);
    }

    @GetMapping("/online-orders")
    public ResponseEntity<byte[]> onlineOrdersReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildOnlineOrdersReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-pedidos-online", dateFrom, dateTo);
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<byte[]> cashFlowReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        if (date == null) {
            date = LocalDate.now();
        }
        byte[] pdf = reportService.buildCashFlowReport(companyId, date);
        return buildPdfResponse(pdf, "relatorio-fluxo-caixa", date, date);
    }

    @GetMapping("/fiado")
    public ResponseEntity<byte[]> fiadoReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @RequestParam(value = "customerId", required = false) String customerId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildFiadoReport(companyId, dateFrom, dateTo, customerId);
        return buildPdfResponse(pdf, "relatorio-fiado", dateFrom, dateTo);
    }

    @GetMapping("/fiado/customer")
    public ResponseEntity<byte[]> customerFiadoReport(
        @RequestParam("companyId") String companyId,
        @RequestParam("customerId") String customerId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        byte[] pdf = reportService.buildCustomerFiadoStatementReport(companyId, customerId);
        return buildPdfResponse(pdf, "relatorio-fiado-cliente", null, null);
    }

    @GetMapping("/customers")
    public ResponseEntity<byte[]> customersReport(
        @RequestParam("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        byte[] pdf = reportService.buildCustomersReport(companyId);
        return buildPdfResponse(pdf, "relatorio-clientes", null, null);
    }

    @GetMapping("/employees")
    public ResponseEntity<byte[]> employeesReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildEmployeesReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-funcionarios", dateFrom, dateTo);
    }

    @GetMapping("/profitability")
    public ResponseEntity<List<ProductProfitabilityDTO>> profitabilityData(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        return ResponseEntity.ok(profitabilityService.getProductProfitability(companyId, dateFrom, dateTo));
    }

    @GetMapping("/profitability/pdf")
    public ResponseEntity<byte[]> profitabilityPdf(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildProfitabilityReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-rentabilidade", dateFrom, dateTo);
    }

    // ── CSV Exports ───────────────────────────────────────────────────────────

    @GetMapping("/{companyId}/orders/csv")
    public ResponseEntity<byte[]> ordersCsv(
        @PathVariable("companyId") String companyId,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        if (dateFrom == null) dateFrom = LocalDate.now().minusMonths(1);
        if (dateTo == null) dateTo = LocalDate.now();
        byte[] csv = csvReportService.buildOrdersCsv(companyId, dateFrom, dateTo);
        return buildCsvResponse(csv, "encomendas_" + dateFrom + "_" + dateTo + ".csv");
    }

    @GetMapping("/{companyId}/cashflow/csv")
    public ResponseEntity<byte[]> cashFlowCsv(
        @PathVariable("companyId") String companyId,
        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        if (date == null) date = LocalDate.now();
        byte[] csv = csvReportService.buildCashFlowCsv(companyId, date);
        return buildCsvResponse(csv, "fluxo_caixa_" + date + ".csv");
    }

    @GetMapping("/{companyId}/profitability/csv")
    public ResponseEntity<byte[]> profitabilityCsv(
        @PathVariable("companyId") String companyId,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserHasCompanyRole(user, companyId, "ADMIN", "MANAGER");
        if (dateFrom == null) dateFrom = LocalDate.now().minusMonths(1);
        if (dateTo == null) dateTo = LocalDate.now();
        byte[] csv = csvReportService.buildProfitabilityCsv(companyId, dateFrom, dateTo);
        return buildCsvResponse(csv, "rentabilidade_" + dateFrom + "_" + dateTo + ".csv");
    }

    @GetMapping("/{companyId}/stock/csv")
    public ResponseEntity<byte[]> stockCsv(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        byte[] csv = csvReportService.buildStockCsv(companyId);
        return buildCsvResponse(csv, "estoque_" + LocalDate.now() + ".csv");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(500).body(Map.of("message", "Erro ao gerar relatório"));
    }

    private void validatePeriod(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Período inválido");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom deve ser menor ou igual a dateTo");
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String baseName, LocalDate dateFrom, LocalDate dateTo) {
        String filename;
        if (dateFrom != null && dateTo != null) {
            filename = String.format("%s_%s_%s.pdf", baseName, dateFrom, dateTo);
        } else {
            filename = String.format("%s.pdf", baseName);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private ResponseEntity<byte[]> buildCsvResponse(byte[] csv, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
