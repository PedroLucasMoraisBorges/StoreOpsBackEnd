package com.store_ops_backend.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.CreateOnlineOrderDTO;
import com.store_ops_backend.models.dtos.MenuResponseDTO;
import com.store_ops_backend.models.dtos.MenuResponseDTO.PaymentMethodDTO;
import com.store_ops_backend.models.dtos.NotificationEventDTO;
import com.store_ops_backend.models.dtos.OnlineOrderTrackingDTO;
import com.store_ops_backend.models.dtos.OrderItemResponseDTO;
import com.store_ops_backend.models.dtos.ProductResponseDTO;
import com.store_ops_backend.models.dtos.ProductVariantDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.repositories.CashRegisterRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PaymentMethodRepository;
import com.store_ops_backend.repositories.StockItemRepository;
import com.store_ops_backend.services.LowStockNotifier;
import com.store_ops_backend.services.OrderNotificationService;
import com.store_ops_backend.services.ProductService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("menu")
public class MenuController {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private ProductService productService;
    @Autowired private OrderNotificationService orderNotificationService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentMethodRepository paymentMethodRepository;
    @Autowired private StockItemRepository stockItemRepository;
    @Autowired private CashRegisterRepository cashRegisterRepository;
    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private LowStockNotifier lowStockNotifier;

    @GetMapping("/{slug}")
    public MenuResponseDTO getMenu(@PathVariable("slug") String slug) {
        Company company = findCompanyBySlug(slug);
        boolean isOpen = cashRegisterRepository.findOpenByCompanyId(company.getId()).isPresent();

        List<ProductResponseDTO> rawProducts = productService.listActive(company.getId());
        List<StockItem> stockItems = stockItemRepository.findByCompanyIdOrderByProductNameAsc(company.getId());

        // Map: productId → quantity (product-level stock, no variant, no component)
        Map<String, BigDecimal> productStockMap = stockItems.stream()
            .filter(s -> s.getVariant() == null && s.getComponentOption() == null)
            .collect(Collectors.toMap(
                s -> s.getProduct().getId(),
                StockItem::getQuantity,
                (a, b) -> a
            ));
        // Map: variantId → quantity (variant-level stock)
        Map<String, BigDecimal> variantStockMap = stockItems.stream()
            .filter(s -> s.getVariant() != null)
            .collect(Collectors.toMap(
                s -> s.getVariant().getId(),
                StockItem::getQuantity,
                (a, b) -> a
            ));
        // Map: componentOptionId → quantity (component-level stock)
        Map<String, BigDecimal> componentOptionStockMap = stockItems.stream()
            .filter(s -> s.getComponentOption() != null)
            .collect(Collectors.toMap(
                s -> s.getComponentOption().getId(),
                StockItem::getQuantity,
                (a, b) -> a
            ));

        List<ProductResponseDTO> products = rawProducts.stream()
            .map(p -> withStock(p, productStockMap, variantStockMap, componentOptionStockMap))
            .toList();

        List<PaymentMethodDTO> paymentMethods = paymentMethodRepository.findAll()
            .stream().map(pm -> new PaymentMethodDTO(pm.getId(), pm.getName(), pm.getCode())).toList();

        return new MenuResponseDTO(company.getId(), company.getName(), company.getPhone(),
            company.getAddress(), products, paymentMethods, isOpen);
    }

    @PostMapping("/{slug}/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OnlineOrderTrackingDTO placeOrder(
        @PathVariable("slug") String slug,
        @RequestBody @Valid CreateOnlineOrderDTO data
    ) {
        Company company = findCompanyBySlug(slug);

        if (cashRegisterRepository.findOpenByCompanyId(company.getId()).isEmpty()) {
            throw new IllegalStateException("O estabelecimento está fechado no momento. Não é possível realizar pedidos.");
        }

        String type = "DELIVERY".equalsIgnoreCase(data.deliveryMode()) ? "DELIVERY" : "PICKUP";
        String notesWithPayment = buildNotes(data.notes(), data.paymentNote());

        Order order = new Order(company, data.customerName(), type, "PENDING",
            OffsetDateTime.now(), data.deliveryAddress(), notesWithPayment);
        orderRepository.save(order);

        for (CreateOnlineOrderDTO.CreateOnlineOrderItemDTO item : data.items()) {
            String unit = item.unit() != null && !item.unit().isBlank() ? item.unit().toUpperCase() : "UN";
            orderItemRepository.save(new OrderItem(order, item.name(), item.quantity(), unit, item.unitPrice(), item.notes()));
        }

        // Debit stock for each item that has productId
        for (CreateOnlineOrderDTO.CreateOnlineOrderItemDTO item : data.items()) {
            if (item.productId() == null || item.productId().isBlank()) continue;
            try {
                StockItem stockItem = null;
                if (item.variantId() != null && !item.variantId().isBlank()) {
                    stockItem = stockItemRepository
                        .findVariantLevelStock(item.productId(), item.variantId(), company.getId())
                        .orElse(null);
                } else {
                    stockItem = stockItemRepository
                        .findProductLevelStock(item.productId(), company.getId())
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
                // Stock debit is best-effort; don't fail the order
            }
        }

        BigDecimal total = data.items().stream()
                .map(i -> i.unitPrice().multiply(i.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        try {
            orderNotificationService.notifyNewOrder(company.getId(), order.getId(),
                    data.customerName(), total, type);
        } catch (Exception e) {
            System.out.println("[SSE] Erro ao notificar pedido: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            eventPublisher.publishEvent(new NotificationEventDTO(
                company.getId(),
                NotificationEventDTO.Type.NEW_ORDER,
                "Nova encomenda de " + data.customerName(),
                "Total: R$ " + total + (type.equals("DELIVERY") ? " • Entrega" : " • Retirada"),
                "/orders",
                "order-" + order.getId(),
                null
            ));
        } catch (Exception ignored) {
            // Push é best-effort; nunca afeta o pedido
        }

        return toTracking(order, data.deliveryMode(), data.paymentNote());
    }

    @GetMapping("/{slug}/orders/{orderId}")
    public OnlineOrderTrackingDTO trackOrder(
        @PathVariable("slug") String slug,
        @PathVariable("orderId") String orderId
    ) {
        Company company = findCompanyBySlug(slug);
        Order order = orderRepository.findByCompanyIdAndOrderId(company.getId(), orderId)
            .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        String deliveryMode = "DELIVERY".equals(order.getType()) ? "DELIVERY" : "PICKUP";
        return toTracking(order, deliveryMode, null);
    }

    private ProductResponseDTO withStock(
        ProductResponseDTO p,
        Map<String, BigDecimal> productStockMap,
        Map<String, BigDecimal> variantStockMap,
        Map<String, BigDecimal> componentOptionStockMap
    ) {
        // Rebuild component groups with stock per option
        List<com.store_ops_backend.models.dtos.ProductComponentGroupDTO> groupsWithStock = p.componentGroups().stream()
            .map(g -> {
                List<com.store_ops_backend.models.dtos.ProductComponentOptionDTO> optsWithStock = g.options().stream()
                    .map(o -> new com.store_ops_backend.models.dtos.ProductComponentOptionDTO(
                        o.id(), o.name(), componentOptionStockMap.get(o.id())
                    ))
                    .toList();
                return new com.store_ops_backend.models.dtos.ProductComponentGroupDTO(
                    g.id(), g.name(), g.maxSelections(), g.required(), g.active(), optsWithStock
                );
            })
            .toList();

        if (p.variants().isEmpty()) {
            BigDecimal qty = productStockMap.get(p.id());
            return new ProductResponseDTO(
                p.id(), p.name(), p.category(), p.unit(),
                p.costPrice(), p.sellPrice(), p.active(), p.imageUrl(), p.createdAt(),
                p.variants(), p.extras(), groupsWithStock, qty
            );
        }
        // Build variants with stock quantities
        List<ProductVariantDTO> variantsWithStock = p.variants().stream()
            .map(v -> {
                BigDecimal qty = variantStockMap.get(v.id());
                return new ProductVariantDTO(v.id(), v.name(), v.priceDelta(), v.active(), qty);
            })
            .toList();
        return new ProductResponseDTO(
            p.id(), p.name(), p.category(), p.unit(),
            p.costPrice(), p.sellPrice(), p.active(), p.imageUrl(), p.createdAt(),
            variantsWithStock, p.extras(), groupsWithStock, null
        );
    }

    private OnlineOrderTrackingDTO toTracking(Order order, String deliveryMode, String paymentNote) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OrderItemResponseDTO> itemDtos = items.stream()
            .map(i -> new OrderItemResponseDTO(i.getId(), i.getName(), i.getQuantity(), i.getUnit(), i.getUnitPrice(), i.getNotes()))
            .toList();
        String customerName = order.getCustomer() != null
            ? order.getCustomer().getName()
            : order.getCustomerName();
        return new OnlineOrderTrackingDTO(
            order.getId(), order.getStatus(), customerName,
            total, order.getCreatedAt(), itemDtos,
            deliveryMode, order.getDeliveryAddress(), paymentNote
        );
    }

    private String buildNotes(String notes, String paymentNote) {
        if (paymentNote != null && !paymentNote.isBlank()) {
            String base = (notes != null && !notes.isBlank()) ? notes + " | " : "";
            return base + "Pagamento: " + paymentNote;
        }
        return notes;
    }

    private Company findCompanyBySlug(String slug) {
        return companyRepository.findBySlug(slug)
            .orElseThrow(() -> new EntityNotFoundException("Cardápio não encontrado"));
    }
}
