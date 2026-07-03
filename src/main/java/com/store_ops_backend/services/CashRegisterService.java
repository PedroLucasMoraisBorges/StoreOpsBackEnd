package com.store_ops_backend.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CashRegisterResponseDTO;
import com.store_ops_backend.models.dtos.OpenCashRegisterDTO;
import com.store_ops_backend.models.entities.CashRegister;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.CashRegisterRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.UserCompanyRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CashRegisterService {

    @Autowired
    private CashRegisterRepository cashRegisterRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Transactional
    public CashRegisterResponseDTO open(String companyId, User user, OpenCashRegisterDTO data) {
        cashRegisterRepository.findOpenByCompanyIdForUpdate(companyId).ifPresent(cr -> {
            throw new IllegalStateException("Já existe um caixa aberto para este estabelecimento.");
        });

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Estabelecimento não encontrado."));

        String shift = detectShift(OffsetDateTime.now());
        CashRegister register = new CashRegister(company, user, shift, data.notes());
        return toDTO(cashRegisterRepository.save(register));
    }

    @Transactional
    public CashRegisterResponseDTO close(String companyId, String registerId) {
        CashRegister register = cashRegisterRepository.findByIdAndCompanyId(registerId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Caixa não encontrado."));

        if ("CLOSED".equals(register.getStatus())) {
            throw new IllegalArgumentException("Este caixa já está fechado.");
        }

        register.setStatus("CLOSED");
        register.setClosedAt(OffsetDateTime.now());
        return toDTO(cashRegisterRepository.save(register));
    }

    @Transactional
    public CashRegisterResponseDTO reopen(String companyId, String registerId, String userId) {
        String role = userCompanyRepository.findByCompanyIdAndUserId(companyId, userId)
            .map(uc -> uc.getRole())
            .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Apenas administradores podem reabrir um caixa fechado.");
        }

        CashRegister register = cashRegisterRepository.findByIdAndCompanyId(registerId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Caixa não encontrado."));

        if ("OPEN".equals(register.getStatus())) {
            throw new IllegalArgumentException("Este caixa já está aberto.");
        }

        register.setStatus("OPEN");
        register.setClosedAt(null);
        return toDTO(cashRegisterRepository.save(register));
    }

    public List<CashRegisterResponseDTO> getAll(String companyId) {
        return cashRegisterRepository.findByCompanyIdOrderByOpenedAtDesc(companyId)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    public CashRegisterResponseDTO getCurrent(String companyId) {
        return cashRegisterRepository.findOpenByCompanyId(companyId)
            .map(this::toDTO)
            .orElseThrow(() -> new EntityNotFoundException("Nenhum caixa aberto no momento."));
    }

    private String detectShift(OffsetDateTime dt) {
        int hour = dt.getHour();
        int minute = dt.getMinute();
        if (hour >= 5 && hour < 12) return "MANHA";
        if (hour >= 13 && (hour < 17 || (hour == 17 && minute < 30))) return "TARDE";
        if (hour >= 18 && hour < 22) return "NOITE";
        return "OUTRO";
    }

    private CashRegisterResponseDTO toDTO(CashRegister cr) {
        String displayName = cr.getUser().getName() != null
            ? cr.getUser().getName()
            : cr.getUser().getUsername();
        return new CashRegisterResponseDTO(
            cr.getId(),
            cr.getCompany().getId(),
            cr.getUser().getId(),
            displayName,
            cr.getShift(),
            cr.getStatus(),
            cr.getOpenedAt(),
            cr.getClosedAt(),
            cr.getNotes()
        );
    }
}
