package com.store_ops_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.PushSubscriptionDTO;
import com.store_ops_backend.models.dtos.UserNotificationPreferencesDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.PushSubscription;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserNotificationPreference;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.PushSubscriptionRepository;
import com.store_ops_backend.repositories.UserNotificationPreferenceRepository;

@Service
public class PushSubscriptionService {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private UserNotificationPreferenceRepository preferenceRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public void subscribe(User user, String companyId, PushSubscriptionDTO data) {
        // Mesmo endpoint registrado por outro usuário (dispositivo compartilhado):
        // remove para não vazar notificações de quem não está mais logado.
        pushSubscriptionRepository.deleteByEndpointAndUserIdNot(data.endpoint(), user.getId());

        pushSubscriptionRepository
            .findByEndpointAndUserIdAndCompanyId(data.endpoint(), user.getId(), companyId)
            .ifPresentOrElse(
                existing -> existing.updateKeys(data.p256dh(), data.auth(), data.userAgent()),
                () -> {
                    Company company = companyRepository.getReferenceById(companyId);
                    pushSubscriptionRepository.save(new PushSubscription(
                        user, company, data.endpoint(), data.p256dh(), data.auth(), data.userAgent()));
                }
            );
    }

    @Transactional
    public void unsubscribe(User user, String companyId, String endpoint) {
        pushSubscriptionRepository.deleteByEndpointAndUserIdAndCompanyId(endpoint, user.getId(), companyId);
    }

    public UserNotificationPreferencesDTO getPreferences(User user, String companyId) {
        return preferenceRepository.findByUserIdAndCompanyId(user.getId(), companyId)
            .map(this::toDTO)
            .orElseGet(() -> toDTO(new UserNotificationPreference(user.getId(), companyId)));
    }

    @Transactional
    public UserNotificationPreferencesDTO updatePreferences(User user, String companyId,
            UserNotificationPreferencesDTO data) {
        UserNotificationPreference pref = preferenceRepository
            .findByUserIdAndCompanyId(user.getId(), companyId)
            .orElseGet(() -> new UserNotificationPreference(user.getId(), companyId));

        pref.update(data.newOrder(), data.accounts(), data.cashRegister(),
            data.lowStock(), data.weeklyReports(), data.email());

        return toDTO(preferenceRepository.save(pref));
    }

    private UserNotificationPreferencesDTO toDTO(UserNotificationPreference p) {
        return new UserNotificationPreferencesDTO(
            p.isNewOrder(), p.isAccounts(), p.isCashRegister(),
            p.isLowStock(), p.isWeeklyReports(), p.isEmail());
    }
}
