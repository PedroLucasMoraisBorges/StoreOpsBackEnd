package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.TableSession;

public interface TableSessionRepository extends JpaRepository<TableSession, String> {
    List<TableSession> findByCompanyIdOrderByOpenedAtDesc(String companyId);
    Optional<TableSession> findFirstByTableIdAndStatusOrderByOpenedAtDesc(String tableId, String status);
    Optional<TableSession> findByIdAndCompanyId(String id, String companyId);

    @org.springframework.data.jpa.repository.Query("""
        select s from table_sessions s
        where s.company.id = :companyId
        and s.status = 'CLOSED'
        and s.closedAt between :from and :to
    """)
    List<TableSession> findClosedByCompanyAndClosedAtBetween(
        @org.springframework.data.repository.query.Param("companyId") String companyId,
        @org.springframework.data.repository.query.Param("from") java.time.OffsetDateTime from,
        @org.springframework.data.repository.query.Param("to") java.time.OffsetDateTime to
    );

    @org.springframework.data.jpa.repository.Query("""
        select s from table_sessions s
        where s.company.id = :companyId
        and s.status = 'CLOSED'
        and s.paidAt is not null
        order by s.closedAt desc
    """)
    List<TableSession> findClosedWithPaymentByCompany(
        @org.springframework.data.repository.query.Param("companyId") String companyId
    );
}
