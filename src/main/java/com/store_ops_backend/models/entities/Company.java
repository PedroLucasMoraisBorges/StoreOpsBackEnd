package com.store_ops_backend.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "companies")
@Entity(name = "companies")

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")

public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String type;
    private String address;
    private String phone;
    private String team_size;
    private String form_of_service;
    private Boolean notification_new_order;
    private Boolean weekly_reports;
    private Boolean notification_in_email;
    private Boolean notification_for_accounts;

    public Company(String name, String type, String address, String phone, String team_size, String form_of_service,Boolean notification_new_order, Boolean weekly_reports, Boolean notification_in_email,Boolean notification_for_accounts) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.phone = phone;
        this.team_size = team_size;
        this.form_of_service = form_of_service;
        this.notification_new_order = notification_new_order;
        this.weekly_reports = weekly_reports;
        this.notification_in_email = notification_in_email;
        this.notification_for_accounts = notification_for_accounts;
    }

    public void update(
        String name,
        String type,
        String address,
        String phone,
        String teamSize,
        String formOfService,
        boolean newOrder,
        boolean weeklyReports,
        boolean email,
        boolean accounts
    ) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.phone = phone;
        this.team_size = teamSize;
        this.form_of_service = formOfService;

        this.notification_new_order = newOrder;
        this.weekly_reports = weeklyReports;
        this.notification_in_email = email;
        this.notification_for_accounts = accounts;
    }

}
