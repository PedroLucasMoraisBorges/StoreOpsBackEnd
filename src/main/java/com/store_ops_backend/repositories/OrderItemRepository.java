package com.store_ops_backend.repositories;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.TopProductRawDTO;
import com.store_ops_backend.models.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
        select i
        from order_items i
        where i.order.id = :orderId
        order by i.id asc
    """)
    List<OrderItem> findByOrderId(@Param("orderId") String orderId);

    @Query("""
        select i
        from order_items i
        where i.order.id in :orderIds
    """)
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<String> orderIds);

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

    @Query("""
        select new com.store_ops_backend.models.dtos.TopProductRawDTO(
            i.name,
            sum(i.quantity),
            sum(i.quantity * i.unitPrice)
        )
        from order_items i
        where i.order.company.id = :companyId
        and i.order.scheduledAt between :from and :to
        and i.order.status = 'COMPLETED'
        group by i.name
        order by sum(i.quantity) desc
    """)
    List<TopProductRawDTO> findTopProductsByCompanyId(
        @Param("companyId") String companyId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        Pageable pageable
    );
}
