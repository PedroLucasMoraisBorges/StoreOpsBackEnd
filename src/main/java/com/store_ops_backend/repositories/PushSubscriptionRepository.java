package com.store_ops_backend.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.entities.PushSubscription;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, String> {

    @Query("""
        select ps
        from push_subscriptions ps
        where ps.company.id = :companyId
        and ps.user.id in :userIds
    """)
    List<PushSubscription> findByCompanyIdAndUserIdIn(
        @Param("companyId") String companyId,
        @Param("userIds") List<String> userIds
    );

    @Query("""
        select ps
        from push_subscriptions ps
        where ps.endpoint = :endpoint
        and ps.user.id = :userId
        and ps.company.id = :companyId
    """)
    Optional<PushSubscription> findByEndpointAndUserIdAndCompanyId(
        @Param("endpoint") String endpoint,
        @Param("userId") String userId,
        @Param("companyId") String companyId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from push_subscriptions ps
        where ps.endpoint = :endpoint
        and ps.user.id <> :userId
    """)
    void deleteByEndpointAndUserIdNot(
        @Param("endpoint") String endpoint,
        @Param("userId") String userId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from push_subscriptions ps
        where ps.endpoint = :endpoint
        and ps.user.id = :userId
        and ps.company.id = :companyId
    """)
    void deleteByEndpointAndUserIdAndCompanyId(
        @Param("endpoint") String endpoint,
        @Param("userId") String userId,
        @Param("companyId") String companyId
    );

    @Modifying
    @Transactional
    @Query("""
        delete from push_subscriptions ps
        where coalesce(ps.lastSuccessAt, ps.createdAt) < :cutoff
    """)
    int deleteStale(@Param("cutoff") OffsetDateTime cutoff);
}
