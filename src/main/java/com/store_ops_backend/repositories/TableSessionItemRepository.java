package com.store_ops_backend.repositories;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.TableSessionItem;

public interface TableSessionItemRepository extends JpaRepository<TableSessionItem, String> {

    @Query("""
        select tsi
        from table_session_items tsi
        where tsi.session.company.id = :companyId
        and tsi.product is not null
        and tsi.session.status = 'CLOSED'
        and tsi.session.closedAt between :from and :to
    """)
    List<TableSessionItem> findSoldItemsWithProductByCompanyAndPeriod(
        @Param("companyId") String companyId,
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );
}
