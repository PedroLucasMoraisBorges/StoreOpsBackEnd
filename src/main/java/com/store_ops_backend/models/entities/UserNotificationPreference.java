package com.store_ops_backend.models.entities;

import java.io.Serializable;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "user_notification_preferences")
@Entity(name = "user_notification_preferences")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class UserNotificationPreference {

    @EmbeddedId
    private UserNotificationPreferenceId id;

    @Column(name = "new_order", nullable = false)
    private boolean newOrder;

    @Column(nullable = false)
    private boolean accounts;

    @Column(name = "cash_register", nullable = false)
    private boolean cashRegister;

    @Column(name = "low_stock", nullable = false)
    private boolean lowStock;

    @Column(name = "weekly_reports", nullable = false)
    private boolean weeklyReports;

    @Column(nullable = false)
    private boolean email;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UserNotificationPreference(String userId, String companyId) {
        this.id = new UserNotificationPreferenceId(userId, companyId);
        this.newOrder = true;
        this.accounts = true;
        this.cashRegister = true;
        this.lowStock = true;
        this.weeklyReports = false;
        this.email = false;
        this.updatedAt = OffsetDateTime.now();
    }

    public String getUserId() {
        return id.getUserId();
    }

    public void update(Boolean newOrder, Boolean accounts, Boolean cashRegister,
            Boolean lowStock, Boolean weeklyReports, Boolean email) {
        if (newOrder != null) this.newOrder = newOrder;
        if (accounts != null) this.accounts = accounts;
        if (cashRegister != null) this.cashRegister = cashRegister;
        if (lowStock != null) this.lowStock = lowStock;
        if (weeklyReports != null) this.weeklyReports = weeklyReports;
        if (email != null) this.email = email;
        this.updatedAt = OffsetDateTime.now();
    }
}

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class UserNotificationPreferenceId implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "company_id")
    private String companyId;
}
