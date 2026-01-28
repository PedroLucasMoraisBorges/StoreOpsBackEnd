package com.store_ops_backend.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.services.reports.ReportService;

@RestController
@RequestMapping("reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/orders")
    public ResponseEntity<byte[]> ordersReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildOrdersReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-encomendas", dateFrom, dateTo);
    }

    @GetMapping("/fiado")
    public ResponseEntity<byte[]> fiadoReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildFiadoReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-fiado", dateFrom, dateTo);
    }

    @GetMapping("/customers")
    public ResponseEntity<byte[]> customersReport(
        @RequestParam("companyId") String companyId
    ) {
        byte[] pdf = reportService.buildCustomersReport(companyId);
        return buildPdfResponse(pdf, "relatorio-clientes", null, null);
    }

    @GetMapping("/employees")
    public ResponseEntity<byte[]> employeesReport(
        @RequestParam("companyId") String companyId,
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        validatePeriod(dateFrom, dateTo);
        byte[] pdf = reportService.buildEmployeesReport(companyId, dateFrom, dateTo);
        return buildPdfResponse(pdf, "relatorio-funcionarios", dateFrom, dateTo);
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
}
