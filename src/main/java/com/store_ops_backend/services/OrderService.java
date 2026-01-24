package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.OrderItemDTO;
import com.store_ops_backend.models.dtos.OrderItemResponseDTO;
import com.store_ops_backend.models.dtos.OrderResponseDTO;
import com.store_ops_backend.models.dtos.CreateOrderDTO;
import com.store_ops_backend.models.dtos.OrderCustomerResponseDTO;
import com.store_ops_backend.models.dtos.UpdateOrderDTO;
import com.store_ops_backend.models.entities.Account;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Order;
import com.store_ops_backend.models.entities.OrderItem;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.repositories.AccountRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.OrderItemRepository;
import com.store_ops_backend.repositories.OrderRepository;
import com.store_ops_backend.repositories.PeopleRepository;

@Service
public class OrderService {
    private static final String CUSTOMER_TYPE = "CLIENT";
    private static final Set<String> UNITS = Set.of("UN", "KG", "G", "L", "ML");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserCompanyService userCompanyService;

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderDTO data, String companyId, String userId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        People customer = resolveCustomer(company, data.customerId(), data.customerName());
        UserCompany attendant = resolveAttendant(companyId, userId);

        System.out.println("iodofidsjfdjfldsfjdslfdsf");
        System.out.println(attendant.getPosition());
        System.out.println(data);

        String type = normalizeType(data.type());
        validateDelivery(type, data.deliveryAddress());

        Order order = new Order(
            company,
            customer,
            attendant,
            type,
            "PENDING",
            requireScheduledAt(data.scheduledAt()),
            data.deliveryAddress(),
            data.notes()
        );

        System.out.println(order.getAttendant().getRole());
        orderRepository.save(order);

        saveItems(order, data.products());

        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    public List<OrderResponseDTO> getAllOrders(String companyId) {
        return orderRepository.findByCompanyId(companyId).stream()
            .map(order -> toResponse(order, orderItemRepository.findByOrderId(order.getId())))
            .toList();
    }

    public OrderResponseDTO getOrderById(String companyId, String orderId) {
        Order order = orderRepository.findByCompanyIdAndOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    public List<OrderCustomerResponseDTO> getOrdersByCustomer(String companyId, String customerId) {
        return orderRepository
            .findByCompanyIdAndCustomerId(companyId, customerId)
            .stream()
            .map(order -> toCustomerResponse(order, orderItemRepository.findByOrderId(order.getId())))
            .toList();
    }

    @Transactional
    public OrderResponseDTO updateOrder(String companyId, String orderId, UpdateOrderDTO data) {
        Order order = orderRepository.findByCompanyIdAndOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        People customer = resolveCustomerUpdate(order.getCompany(), data.customerId(), data.customerName());
        UserCompany attendant = resolveAttendantOptional(companyId, data.attendantUserId());
        String type = data.type() == null ? null : normalizeType(data.type());

        if (type != null) {
            String address = data.deliveryAddress() != null ? data.deliveryAddress() : order.getDeliveryAddress();
            validateDelivery(type, address);
        }

        order.update(
            customer,
            attendant,
            type,
            data.scheduledAt(),
            data.deliveryAddress(),
            data.notes()
        );
        orderRepository.save(order);

        if (data.products() != null) {
            orderItemRepository.deleteByOrderId(order.getId());
            saveItems(order, data.products());
        }

        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderResponseDTO updateStatus(String companyId, String orderId, String status) {
        Order order = orderRepository.findByCompanyIdAndOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        order.updateStatus(normalizeStatus(status));
        orderRepository.save(order);
        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderResponseDTO addItems(String companyId, String orderId, List<OrderItemDTO> products) {
        Order order = orderRepository.findByCompanyIdAndOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        saveItems(order, products);
        order.update(null, null, null, null, null, null);
        orderRepository.save(order);
        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderResponseDTO removeItem(String companyId, String orderId, String itemId) {
        Order order = orderRepository.findByCompanyIdAndOrderId(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        orderItemRepository.deleteByIdAndOrderId(itemId, order.getId());
        order.update(null, null, null, null, null, null);
        orderRepository.save(order);
        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    private void saveItems(Order order, List<OrderItemDTO> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        products.forEach(item -> {
            validateItem(item);
            OrderItem orderItem = new OrderItem(
                order,
                item.name(),
                item.quantity(),
                normalizeUnit(item.unit()),
                item.unitPrice()
            );
            orderItemRepository.save(orderItem);
        });
    }

    private void validateItem(OrderItemDTO item) {
        if (item == null || item.name() == null || item.name().isBlank()) {
            throw new RuntimeException("Product name is required");
        }
        if (item.quantity() == null || item.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Product quantity must be greater than zero");
        }
        if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Product unit price must be greater than zero");
        }
        normalizeUnit(item.unit());
    }

    private String normalizeUnit(String unit) {
        if (unit == null) {
            throw new RuntimeException("Product unit is required");
        }
        String normalized = unit.trim().toUpperCase();
        if (!UNITS.contains(normalized)) {
            throw new RuntimeException("Invalid unit");
        }
        return normalized;
    }

    private People resolveCustomer(Company company, String customerId, String customerName) {
        if (customerId != null && !customerId.isBlank()) {
            return peopleRepository
                .findByCompanyIdAndPersonIdAndType(company.getId(), customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        }

        if (customerName == null || customerName.isBlank()) {
            throw new RuntimeException("Customer name is required");
        }

        People people = new People(
            customerName,
            CUSTOMER_TYPE,
            company,
            null,
            null,
            null,
            true
        );
        peopleRepository.save(people);

        Account account = new Account(null, "OPEN", OffsetDateTime.now(), null, people, company);
        accountRepository.save(account);
        return people;
    }

    private People resolveCustomerUpdate(Company company, String customerId, String customerName) {
        if ((customerId == null || customerId.isBlank()) && (customerName == null || customerName.isBlank())) {
            return null;
        }
        return resolveCustomer(company, customerId, customerName);
    }

    private UserCompany resolveAttendant(String companyId, String attendantUserId) {
        if (attendantUserId == null || attendantUserId.isBlank()) {
            throw new RuntimeException("Attendant user is required");
        }
        return userCompanyService.getUserCompany(companyId, attendantUserId);
    }

    private UserCompany resolveAttendantOptional(String companyId, String attendantUserId) {
        if (attendantUserId == null || attendantUserId.isBlank()) {
            return null;
        }
        return userCompanyService.getUserCompany(companyId, attendantUserId);
    }

    private OffsetDateTime requireScheduledAt(OffsetDateTime scheduledAt) {
        if (scheduledAt == null) {
            throw new RuntimeException("Scheduled date is required");
        }
        return scheduledAt;
    }

    private void validateDelivery(String type, String deliveryAddress) {
        if ("DELIVERY".equals(type) && (deliveryAddress == null || deliveryAddress.isBlank())) {
            throw new RuntimeException("Delivery address is required");
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new RuntimeException("Order type is required");
        }
        String normalized = type.trim().toUpperCase();
        if (normalized.equals("RETIRADA")) {
            return "PICKUP";
        }
        if (normalized.equals("ENTREGA")) {
            return "DELIVERY";
        }
        if (normalized.equals("PICKUP") || normalized.equals("DELIVERY")) {
            return normalized;
        }
        throw new RuntimeException("Invalid order type");
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required");
        }
        String normalized = status.trim().toUpperCase();
        if (normalized.equals("PENDENTE") || normalized.equals("PENDING")) {
            return "PENDING";
        }
        if (normalized.equals("AGUARDANDO_RETIRADA") || normalized.equals("WAITING_PICKUP")) {
            return "WAITING_PICKUP";
        }
        if (normalized.equals("CONCLUIDA") || normalized.equals("CONCLUÍDA") || normalized.equals("COMPLETED")) {
            return "COMPLETED";
        }
        if (normalized.equals("CANCELADA") || normalized.equals("CANCELED")) {
            return "CANCELED";
        }
        throw new RuntimeException("Invalid status");
    }

    private OrderResponseDTO toResponse(Order order, List<OrderItem> items) {
        String attendantUserId = order.getAttendant() != null
            ? order.getAttendant().getUser().getId()
            : order.getAttendantUserId();
        return new OrderResponseDTO(
            order.getId(),
            order.getCustomer().getId(),
            order.getCustomer().getName(),
            attendantUserId,
            order.getType(),
            order.getScheduledAt(),
            order.getDeliveryAddress(),
            order.getNotes(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items.stream().map(this::toItemResponse).toList()
        );
    }

    private OrderItemResponseDTO toItemResponse(OrderItem item) {
        return new OrderItemResponseDTO(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getUnit(),
            item.getUnitPrice()
        );
    }

    private OrderCustomerResponseDTO toCustomerResponse(Order order, List<OrderItem> items) {
        String attendantUserId = order.getAttendant() != null
            ? order.getAttendant().getUser().getId()
            : order.getAttendantUserId();

        return new OrderCustomerResponseDTO(
            order.getId(),
            attendantUserId,
            order.getType(),
            order.getScheduledAt(),
            order.getDeliveryAddress(),
            order.getNotes(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            items.stream().map(this::toItemResponse).toList()
        );
    }
}
