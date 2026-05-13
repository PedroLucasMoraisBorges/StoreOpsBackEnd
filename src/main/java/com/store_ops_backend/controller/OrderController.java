package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.dtos.AddOrderItemsDTO;
import com.store_ops_backend.models.dtos.CreateOrderDTO;
import jakarta.validation.Valid;
import com.store_ops_backend.models.dtos.OrderResponseDTO;
import com.store_ops_backend.models.dtos.UpdateOrderDTO;
import com.store_ops_backend.models.dtos.UpdateOrderStatusDTO;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.services.OrderService;

@RestController
@RequestMapping("orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthorizationHelper authorizationHelper;

    @PostMapping("/create/{companyId}")
    public OrderResponseDTO createOrder(
        @RequestBody @Valid CreateOrderDTO data,
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.createOrder(data, companyId, user.getId());
    }

    @GetMapping("/getAll/{companyId}")
    public List<OrderResponseDTO> getAllOrders(
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.getAllOrders(companyId);
    }

    @GetMapping("/get/{companyId}/{orderId}")
    public OrderResponseDTO getOrderById(
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.getOrderById(companyId, orderId);
    }

    @GetMapping("/getByCustomer/{companyId}/{customerId}")
    public List<OrderResponseDTO> getOrdersByCustomer(
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.getOrdersByCustomer(companyId, customerId);
    }

    @PutMapping("/update/{companyId}/{orderId}")
    public OrderResponseDTO updateOrder(
        @RequestBody UpdateOrderDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.updateOrder(companyId, orderId, data);
    }

    @PutMapping("/status/{companyId}/{orderId}")
    public OrderResponseDTO updateStatus(
        @RequestBody UpdateOrderStatusDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.updateStatus(companyId, orderId, data.status());
    }

    @PostMapping("/items/{companyId}/{orderId}")
    public OrderResponseDTO addItems(
        @RequestBody AddOrderItemsDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.addItems(companyId, orderId, data.products());
    }

    @PutMapping("/items/remove/{companyId}/{orderId}/{itemId}")
    public OrderResponseDTO removeItem(
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @PathVariable("itemId") String itemId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.removeItem(companyId, orderId, itemId);
    }

    @PutMapping("/payment/{companyId}/{orderId}")
    public OrderResponseDTO recordPayment(
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @RequestParam("paymentMethodId") String paymentMethodId,
        @AuthenticationPrincipal User user
    ) {
        authorizationHelper.assertUserBelongsToCompany(user, companyId);
        return orderService.recordPayment(companyId, orderId, paymentMethodId);
    }
}
