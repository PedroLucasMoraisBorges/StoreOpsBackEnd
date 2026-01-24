package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        order by o.createdAt desc
    """)
    List<Order> findByCompanyId(@Param("companyId") String companyId);

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        and o.id = :orderId
    """)
    Optional<Order> findByCompanyIdAndOrderId(
        @Param("companyId") String companyId,
        @Param("orderId") String orderId
    );

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        and o.customer.id = :customerId
        order by o.createdAt desc
    """)
    List<Order> findByCompanyIdAndCustomerId(
        @Param("companyId") String companyId,
        @Param("customerId") String customerId
    );
}
