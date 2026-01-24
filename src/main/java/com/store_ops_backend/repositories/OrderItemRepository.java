package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
        select i
        from order_items i
        where i.order.id = :orderId
        order by i.id asc
    """)
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Transactional
    @Query("""
        delete from order_items i
        where i.order.id = :orderId
    """)
    void deleteByOrderId(@Param("orderId") String orderId);

    @Modifying
    @Transactional
    @Query("""
        delete from order_items i
        where i.id = :itemId
        and i.order.id = :orderId
    """)
    void deleteByIdAndOrderId(
        @Param("itemId") String itemId,
        @Param("orderId") String orderId
    );
}
