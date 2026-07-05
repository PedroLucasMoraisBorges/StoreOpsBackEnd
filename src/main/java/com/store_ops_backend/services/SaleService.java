package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateCounterSaleDTO;
import com.store_ops_backend.models.dtos.OrderItemResponseDTO;
import com.store_ops_backend.models.dtos.OrderResponseDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.PaymentMethods;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.repositories.CashRegisterRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PaymentMethodRepository;
import com.store_ops_backend.repositories.StockItemRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleService {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private PaymentMethodRepository paymentMethodRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private StockItemRepository stockItemRepository;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private LowStockNotifier lowStockNotifier;

    @Transactional
    public OrderResponseDTO createCounterSale(CreateCounterSaleDTO data, String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        if (cashRegisterRepository.findOpenByCompanyId(companyId).isEmpty()) {
            throw new IllegalStateException("Caixa fechado. Abra o caixa antes de registrar uma venda.");
        }

        PaymentMethods paymentMethod = paymentMethodRepository.findById(data.paymentMethodId())
            .orElseThrow(() -> new EntityNotFoundException("Forma de pagamento não encontrada"));

        String customerName = (data.customerName() != null && !data.customerName().isBlank())
            ? data.customerName().trim()
            : "Balcão";

        Order order = new Order(
            company,
            customerName,
            "COUNTER",
            "COMPLETED",
            OffsetDateTime.now(),
            null,
            data.notes()
        );
        order.recordPayment(paymentMethod);
        orderRepository.save(order);

        for (CreateCounterSaleDTO.CounterSaleItemDTO item : data.items()) {
            String unit = item.unit() != null && !item.unit().isBlank()
                ? item.unit().trim().toUpperCase()
                : "UN";
            orderItemRepository.save(new OrderItem(order, item.name(), item.quantity(), unit, item.unitPrice(), item.notes()));
        }

        // Debit stock for each item that links to a product — best-effort
        for (CreateCounterSaleDTO.CounterSaleItemDTO item : data.items()) {
            if (item.productId() == null || item.productId().isBlank()) continue;
            try {
                StockItem stockItem = null;
                if (item.variantId() != null && !item.variantId().isBlank()) {
                    stockItem = stockItemRepository
                        .findVariantLevelStock(item.productId(), item.variantId(), companyId)
                        .orElse(null);
                } else {
                    stockItem = stockItemRepository
                        .findProductLevelStock(item.productId(), companyId)
                        .orElse(null);
                }
                if (stockItem != null) {
                    BigDecimal newQty = stockItem.getQuantity().subtract(item.quantity());
                    if (newQty.compareTo(BigDecimal.ZERO) >= 0) {
                        boolean wasBelow = lowStockNotifier.isBelow(stockItem);
                        stockItem.applyMovement(item.quantity().negate());
                        stockItemRepository.save(stockItem);
                        lowStockNotifier.notifyIfCrossedMinimum(stockItem, wasBelow);
                    }
                }
            } catch (Exception ignored) {
                // Stock debit is best-effort; don't fail the sale
            }
        }

        List<OrderItem> savedItems = orderItemRepository.findByOrderId(order.getId());
        return toResponse(order, savedItems);
    }

    private OrderResponseDTO toResponse(Order order, List<OrderItem> items) {
        String paymentMethodId   = order.getPaymentMethod() != null ? order.getPaymentMethod().getId()   : null;
        String paymentMethodName = order.getPaymentMethod() != null ? order.getPaymentMethod().getName() : null;
        String customerName      = order.getCustomer()      != null ? order.getCustomer().getName()      : order.getCustomerName();
        return new OrderResponseDTO(
            order.getId(),
            null,
            customerName,
            null,
            order.getType(),
            order.getScheduledAt(),
            order.getDeliveryAddress(),
            order.getNotes(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items.stream().map(this::toItemResponse).toList(),
            paymentMethodId,
            paymentMethodName,
            order.getPaidAt()
        );
    }

    private OrderItemResponseDTO toItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.getUnitPrice(),
            item.getNotes()
        );
    }
}
