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
import org.springframework.web.bind.annotation.RestController;

import com.store_ops_backend.models.dtos.AddOrderItemsDTO;
import com.store_ops_backend.models.dtos.CreateOrderDTO;
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

    @PostMapping("/create/{companyId}")
    public OrderResponseDTO createOrder(
        @RequestBody CreateOrderDTO data,
        @PathVariable("companyId") String companyId,
        @AuthenticationPrincipal User user
    ) {
        return orderService.createOrder(data, companyId, user.getId());
    }

    @GetMapping("/getAll/{companyId}")
    public List<OrderResponseDTO> getAllOrders(@PathVariable("companyId") String companyId) {
        return orderService.getAllOrders(companyId);
    }

    @GetMapping("/get/{companyId}/{orderId}")
    public OrderResponseDTO getOrderById(
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId
    ) {
        return orderService.getOrderById(companyId, orderId);
    }

    @GetMapping("/getByCustomer/{companyId}/{customerId}")
    public List<OrderResponseDTO> getOrdersByCustomer(
        @PathVariable("companyId") String companyId,
        @PathVariable("customerId") String customerId
    ) {
        return orderService.getOrdersByCustomer(companyId, customerId);
    }

    @PutMapping("/update/{companyId}/{orderId}")
    public OrderResponseDTO updateOrder(
        @RequestBody UpdateOrderDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId
    ) {
        return orderService.updateOrder(companyId, orderId, data);
    }

    @PutMapping("/status/{companyId}/{orderId}")
    public OrderResponseDTO updateStatus(
        @RequestBody UpdateOrderStatusDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId
    ) {
        return orderService.updateStatus(companyId, orderId, data.status());
    }

    @PostMapping("/items/{companyId}/{orderId}")
    public OrderResponseDTO addItems(
        @RequestBody AddOrderItemsDTO data,
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId
    ) {
        return orderService.addItems(companyId, orderId, data.products());
    }

    @PutMapping("/items/remove/{companyId}/{orderId}/{itemId}")
    public OrderResponseDTO removeItem(
        @PathVariable("companyId") String companyId,
        @PathVariable("orderId") String orderId,
        @PathVariable("itemId") String itemId
    ) {
        return orderService.removeItem(companyId, orderId, itemId);
    }
}
