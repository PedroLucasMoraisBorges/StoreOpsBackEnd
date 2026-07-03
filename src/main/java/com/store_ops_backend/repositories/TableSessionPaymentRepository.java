package com.store_ops_backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_ops_backend.models.entities.TableSessionPayment;

public interface TableSessionPaymentRepository extends JpaRepository<TableSessionPayment, String> {
    List<TableSessionPayment> findBySessionId(String sessionId);
}
