package com.store_ops_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.AddSessionItemDTO;
import com.store_ops_backend.models.dtos.CreateTableDTO;
import com.store_ops_backend.models.dtos.TableResponseDTO;
import com.store_ops_backend.models.dtos.TableSessionResponseDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.TableService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("tables")
public class TableController {

    @Autowired
    private TableService tableService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @GetMapping("/getAll/{companyId}")
    public List<TableResponseDTO> getAll(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.listTables(companyId);
    }

    @PostMapping("/create/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponseDTO create(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid CreateTableDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.createTable(companyId, data);
    }

    @DeleteMapping("/delete/{companyId}/{tableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable("companyId") String companyId,
        @PathVariable("tableId") String tableId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        tableService.deleteTable(companyId, tableId);
    }

    @PostMapping("/sessions/open/{companyId}/{tableId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TableSessionResponseDTO openSession(
        @PathVariable("companyId") String companyId,
        @PathVariable("tableId") String tableId,
        @RequestBody(required = false) Map<String, String> body,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        String notes = body != null ? body.get("notes") : null;
        return tableService.openSession(companyId, tableId, notes);
    }

    @GetMapping("/sessions/open/{companyId}/{tableId}")
    public TableSessionResponseDTO getOpenSession(
        @PathVariable("companyId") String companyId,
        @PathVariable("tableId") String tableId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.getOpenSession(companyId, tableId);
    }

    @GetMapping("/sessions/get/{companyId}/{sessionId}")
    public TableSessionResponseDTO getSession(
        @PathVariable("companyId") String companyId,
        @PathVariable("sessionId") String sessionId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.getSession(companyId, sessionId);
    }

    @PostMapping("/sessions/items/{companyId}/{sessionId}")
    public TableSessionResponseDTO addItem(
        @PathVariable("companyId") String companyId,
        @PathVariable("sessionId") String sessionId,
        @RequestBody @Valid AddSessionItemDTO data,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.addItem(companyId, sessionId, data);
    }

    @DeleteMapping("/sessions/items/{companyId}/{sessionId}/{itemId}")
    public TableSessionResponseDTO removeItem(
        @PathVariable("companyId") String companyId,
        @PathVariable("sessionId") String sessionId,
        @PathVariable("itemId") String itemId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.removeItem(companyId, sessionId, itemId);
    }

    @PostMapping("/sessions/close/{companyId}/{sessionId}")
    public TableSessionResponseDTO closeSession(
        @PathVariable("companyId") String companyId,
        @PathVariable("sessionId") String sessionId,
        @RequestBody(required = false) Map<String, String> body,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        String paymentMethodId = body != null ? body.get("paymentMethodId") : null;
        return tableService.closeSession(companyId, sessionId, paymentMethodId);
    }

    @GetMapping("/sessions/payments/{companyId}")
    public List<TableSessionResponseDTO> getClosedSessionsWithPayment(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return tableService.listClosedSessionsWithPayment(companyId);
    }
}
