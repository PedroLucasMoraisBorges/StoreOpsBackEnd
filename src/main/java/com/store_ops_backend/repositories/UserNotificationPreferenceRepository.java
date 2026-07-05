package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.UserNotificationPreference;

public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, String> {

    @Query("""
        select p
        from user_notification_preferences p
        where p.id.userId = :userId
        and p.id.companyId = :companyId
    """)
    Optional<UserNotificationPreference> findByUserIdAndCompanyId(
        @Param("userId") String userId,
        @Param("companyId") String companyId
    );

    @Query("""
        select p
        from user_notification_preferences p
        where p.id.companyId = :companyId
        and p.id.userId in :userIds
    """)
    List<UserNotificationPreference> findByCompanyIdAndUserIdIn(
        @Param("companyId") String companyId,
        @Param("userIds") List<String> userIds
    );
}
