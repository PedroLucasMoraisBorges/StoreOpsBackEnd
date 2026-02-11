package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        and o.scheduledAt between :dateFrom and :dateTo
        order by o.scheduledAt desc
    """)
    List<Order> findByCompanyIdAndScheduledAtBetween(
        @Param("companyId") String companyId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        and o.attendantUserId = :userId
        and o.createdAt between :dateFrom and :dateTo
        order by o.createdAt desc
    """)
    List<Order> findByCompanyIdAndAttendantUserIdAndCreatedAtBetween(
        @Param("companyId") String companyId,
        @Param("userId") String userId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        and o.status = 'COMPLETED'
        and o.scheduledAt between :dateFrom and :dateTo
        order by o.scheduledAt desc
    """)
    List<Order> findCompletedByCompanyIdAndCreatedAtBetween(
        @Param("companyId") String companyId,
        @Param("dateFrom") java.time.OffsetDateTime dateFrom,
        @Param("dateTo") java.time.OffsetDateTime dateTo
    );

    @Query("""
        select o
        from orders o
        where o.company.id = :companyId
        order by o.scheduledAt desc
    """)
    List<Order> findRecentByCompanyId(org.springframework.data.domain.Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        delete from orders o
        where o.company.id = :companyId
    """)
    void deleteByCompanyId(@Param("companyId") String companyId);
}
