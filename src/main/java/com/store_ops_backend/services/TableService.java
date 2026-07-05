package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.AddSessionItemDTO;
import com.store_ops_backend.models.dtos.CloseSessionDTO;
import com.store_ops_backend.models.dtos.CreateTableDTO;
import com.store_ops_backend.models.dtos.PaymentSplitDTO;
import com.store_ops_backend.models.dtos.PaymentSplitResponseDTO;
import com.store_ops_backend.models.dtos.SessionItemResponseDTO;
import com.store_ops_backend.models.dtos.TableResponseDTO;
import com.store_ops_backend.models.dtos.TableSessionResponseDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.PaymentMethods;
import com.store_ops_backend.models.entities.Product;
import com.store_ops_backend.models.entities.ProductVariant;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.models.entities.StoreTable;
import com.store_ops_backend.models.entities.TableSession;
import com.store_ops_backend.models.entities.TableSessionItem;
import com.store_ops_backend.models.entities.TableSessionPayment;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.PaymentMethodRepository;
import com.store_ops_backend.repositories.ProductRepository;
import com.store_ops_backend.repositories.ProductVariantRepository;
import com.store_ops_backend.repositories.StockItemRepository;
import com.store_ops_backend.repositories.StoreTableRepository;
import com.store_ops_backend.repositories.TableSessionPaymentRepository;
import com.store_ops_backend.repositories.TableSessionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TableService {

    @Autowired private StoreTableRepository tableRepository;
    @Autowired private TableSessionRepository sessionRepository;
    @Autowired private TableSessionPaymentRepository sessionPaymentRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PaymentMethodRepository paymentMethodRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private StockItemRepository stockItemRepository;
    @Autowired private LowStockNotifier lowStockNotifier;

    public List<TableResponseDTO> listTables(String companyId) {
        return tableRepository.findByCompanyIdOrderByNumberAsc(companyId)
            .stream().map(this::toTableResponse).toList();
    }

    public TableResponseDTO createTable(String companyId, CreateTableDTO data) {
        Company company = findCompany(companyId);
        if (tableRepository.existsByCompanyIdAndNumber(companyId, data.number())) {
            throw new IllegalArgumentException("Já existe uma mesa com o número " + data.number());
        }
        StoreTable table = new StoreTable(company, data.number(), data.sector(), data.capacity());
        return toTableResponse(tableRepository.save(table));
    }

    public void deleteTable(String companyId, String tableId) {
        StoreTable table = findTableOrThrow(companyId, tableId);
        if ("OCCUPIED".equals(table.getStatus())) {
            throw new IllegalArgumentException("Não é possível excluir uma mesa ocupada");
        }
        tableRepository.delete(table);
    }

    @Transactional
    public TableSessionResponseDTO openSession(String companyId, String tableId, String notes) {
        StoreTable table = tableRepository.findByIdAndCompanyIdForUpdate(tableId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada"));
        if (!"FREE".equals(table.getStatus())) {
            throw new IllegalArgumentException("Mesa já está ocupada ou aguardando pagamento");
        }
        Company company = findCompany(companyId);
        table.updateStatus("OCCUPIED");
        tableRepository.save(table);
        TableSession session = new TableSession(table, company, notes);
        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public TableSessionResponseDTO addItem(String companyId, String sessionId, AddSessionItemDTO data) {
        TableSession session = findSessionOrThrow(companyId, sessionId);
        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalArgumentException("Sessão já foi encerrada");
        }
        String unit = data.unit() != null ? data.unit() : "un";

        Product product = null;
        ProductVariant variant = null;

        if (data.productId() != null && !data.productId().isBlank()) {
            product = productRepository.findByIdAndCompanyId(data.productId(), companyId).orElse(null);
        }
        if (product != null && data.variantId() != null && !data.variantId().isBlank()) {
            variant = variantRepository.findById(data.variantId()).orElse(null);
        }

        deductStock(companyId, product, variant, data.quantity());

        TableSessionItem item = new TableSessionItem(session, data.name(), data.quantity(), unit, data.unitPrice(), product, variant);
        session.addItem(item);
        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public TableSessionResponseDTO removeItem(String companyId, String sessionId, String itemId) {
        TableSession session = findSessionOrThrow(companyId, sessionId);
        TableSessionItem itemToRemove = session.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElse(null);

        if (itemToRemove != null) {
            restoreStock(companyId, itemToRemove.getProduct(), itemToRemove.getVariant(), itemToRemove.getQuantity());
            session.getItems().removeIf(i -> i.getId().equals(itemId));
        }

        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public TableSessionResponseDTO closeSession(String companyId, String sessionId, CloseSessionDTO dto) {
        TableSession session = findSessionOrThrow(companyId, sessionId);
        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalStateException("Sessão já foi encerrada");
        }

        List<PaymentSplitDTO> splits = (dto != null && dto.payments() != null) ? dto.payments() : List.of();

        // Determine primary payment method (highest amount), for backward compat on table_sessions.payment_method_id
        PaymentMethods primaryPm = null;
        if (!splits.isEmpty()) {
            String primaryPmId = splits.stream()
                .max(Comparator.comparing(PaymentSplitDTO::amount))
                .map(PaymentSplitDTO::paymentMethodId)
                .orElse(null);
            if (primaryPmId != null) {
                primaryPm = paymentMethodRepository.findById(primaryPmId)
                    .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada"));
            }
        }

        if (primaryPm != null) {
            session.closeWithPayment(primaryPm);
        } else {
            session.close();
        }

        session.getTable().updateStatus("FREE");
        tableRepository.save(session.getTable());
        sessionRepository.save(session);

        // Save individual payment records
        for (PaymentSplitDTO split : splits) {
            PaymentMethods pm = paymentMethodRepository.findById(split.paymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada: " + split.paymentMethodId()));
            sessionPaymentRepository.save(new TableSessionPayment(session, pm, split.amount()));
        }

        return toSessionResponse(session);
    }

    public Map<String, Object> getSplitAmount(String companyId, String sessionId, int persons) {
        if (persons < 1) throw new IllegalArgumentException("Número de pessoas deve ser ao menos 1");
        TableSession session = findSessionOrThrow(companyId, sessionId);
        BigDecimal total = session.getTotal();
        BigDecimal perPerson = total.divide(BigDecimal.valueOf(persons), 2, RoundingMode.HALF_UP);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("perPerson", perPerson);
        result.put("persons", persons);
        return result;
    }

    public List<TableSessionResponseDTO> listClosedSessionsWithPayment(String companyId) {
        return sessionRepository.findClosedWithPaymentByCompany(companyId)
            .stream().map(this::toSessionResponse).toList();
    }

    public TableSessionResponseDTO getSession(String companyId, String sessionId) {
        return toSessionResponse(findSessionOrThrow(companyId, sessionId));
    }

    public TableSessionResponseDTO getOpenSession(String companyId, String tableId) {
        return sessionRepository.findFirstByTableIdAndStatusOrderByOpenedAtDesc(tableId, "OPEN")
            .map(this::toSessionResponse)
            .orElseThrow(() -> new EntityNotFoundException("Nenhuma sessão aberta para esta mesa"));
    }

    private void deductStock(String companyId, Product product, ProductVariant variant, BigDecimal quantity) {
        if (product == null) return;
        try {
            StockItem stockItem = variant != null
                ? stockItemRepository.findVariantLevelStock(product.getId(), variant.getId(), companyId).orElse(null)
                : stockItemRepository.findProductLevelStock(product.getId(), companyId).orElse(null);
            if (stockItem != null) {
                BigDecimal newQty = stockItem.getQuantity().subtract(quantity);
                if (newQty.compareTo(BigDecimal.ZERO) >= 0) {
                    boolean wasBelow = lowStockNotifier.isBelow(stockItem);
                    stockItem.applyMovement(quantity.negate());
                    stockItemRepository.save(stockItem);
                    lowStockNotifier.notifyIfCrossedMinimum(stockItem, wasBelow);
                }
            }
        } catch (Exception ignored) {
            // Stock debit is best-effort
        }
    }

    private void restoreStock(String companyId, Product product, ProductVariant variant, BigDecimal quantity) {
        if (product == null) return;
        try {
            StockItem stockItem = variant != null
                ? stockItemRepository.findVariantLevelStock(product.getId(), variant.getId(), companyId).orElse(null)
                : stockItemRepository.findProductLevelStock(product.getId(), companyId).orElse(null);
            if (stockItem != null) {
                stockItem.applyMovement(quantity);
                stockItemRepository.save(stockItem);
            }
        } catch (Exception ignored) {
            // Stock restore is best-effort
        }
    }

    private StoreTable findTableOrThrow(String companyId, String tableId) {
        return tableRepository.findByIdAndCompanyId(tableId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Mesa não encontrada"));
    }

    private TableSession findSessionOrThrow(String companyId, String sessionId) {
        return sessionRepository.findByIdAndCompanyId(sessionId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Sessão não encontrada"));
    }

    private Company findCompany(String companyId) {
        return companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
    }

    private TableResponseDTO toTableResponse(StoreTable t) {
        return new TableResponseDTO(t.getId(), t.getNumber(), t.getSector(), t.getCapacity(), t.getStatus());
    }

    private TableSessionResponseDTO toSessionResponse(TableSession s) {
        List<SessionItemResponseDTO> items = s.getItems().stream()
            .map(i -> new SessionItemResponseDTO(
                i.getId(), i.getName(), i.getQuantity(), i.getUnit(),
                i.getUnitPrice(), i.getUnitPrice().multiply(i.getQuantity()), i.getAddedAt()
            )).toList();

        List<PaymentSplitResponseDTO> payments = sessionPaymentRepository.findBySessionId(s.getId())
            .stream()
            .map(p -> new PaymentSplitResponseDTO(
                p.getPaymentMethod().getId(),
                p.getPaymentMethod().getName(),
                p.getAmount(),
                p.getPaidAt()
            )).toList();

        String pmId   = s.getPaymentMethod() != null ? s.getPaymentMethod().getId()   : null;
        String pmName = s.getPaymentMethod() != null ? s.getPaymentMethod().getName() : null;
        return new TableSessionResponseDTO(
            s.getId(), s.getTable().getNumber(), s.getTable().getSector(),
            s.getStatus(), s.getNotes(), s.getOpenedAt(), s.getClosedAt(),
            items, s.getTotal(), pmId, pmName, s.getPaidAt(), payments
        );
    }
}
