package com.store_ops_backend.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.CompanyTransactionResponseDTO;
import com.store_ops_backend.models.dtos.CreateTransactionDTO;
import com.store_ops_backend.models.dtos.NotificationEventDTO;
import com.store_ops_backend.models.dtos.TransactionResponseDTO;
import com.store_ops_backend.models.entities.Account;
import com.store_ops_backend.models.entities.AccountTransactions;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.AccountTransactionsRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AccountTransactionService {
    @Autowired
    private AccountTransactionsRepository transactionsRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public TransactionResponseDTO createDebit(String companyId, String customerId, CreateTransactionDTO data, User user) {
        return createTransaction(companyId, customerId, data, user, "CUSTOMER_DEBIT");
    }

    public TransactionResponseDTO createPayment(String companyId, String customerId, CreateTransactionDTO data, User user) {
        return createTransaction(companyId, customerId, data, user, "CUSTOMER_PAYMENT");
    }

    public List<TransactionResponseDTO> listTransactions(String companyId, String customerId) {
        People customer = customerService.findPersonByCustomerOrEmployee(companyId, customerId);
        Account account = customerService.findCustomerAccount(companyId, customer.getId());

        return transactionsRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private TransactionResponseDTO createTransaction(
        String companyId,
        String customerId,
        CreateTransactionDTO data,
        User user,
        String origin
    ) {
        if (data.amount() == null || data.amount().signum() <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        People person = customerService.findPersonByCustomerOrEmployee(companyId, customerId);

        Account account = customerService.findCustomerAccount(companyId, person.getId());

        AccountTransactions transaction = new AccountTransactions(
            null,
            origin,
            data.amount(),
            data.description(),
            OffsetDateTime.now(),
            account,
            user
        );

        transactionsRepository.save(transaction);

        try {
            boolean isDebit = "CUSTOMER_DEBIT".equals(origin);
            eventPublisher.publishEvent(new NotificationEventDTO(
                companyId,
                isDebit ? NotificationEventDTO.Type.FIADO_DEBIT : NotificationEventDTO.Type.FIADO_PAYMENT,
                isDebit ? "Novo fiado: " + person.getName() : "Pagamento de fiado: " + person.getName(),
                "Valor: R$ " + data.amount()
                    + (data.description() != null && !data.description().isBlank() ? " • " + data.description() : ""),
                "/customers",
                "fiado-" + transaction.getId(),
                user != null ? user.getId() : null
            ));
        } catch (Exception ignored) {
            // Notificação é best-effort; nunca afeta a transação
        }

        return toResponse(transaction);
    }

    public List<CompanyTransactionResponseDTO> listByCompany(String companyId) {
        return transactionsRepository
            .findByCompanyId(companyId)
            .stream()
            .map(this::toCompanyResponse)
            .toList();
    }

    private TransactionResponseDTO toResponse(AccountTransactions transaction) {
        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getOrigin(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getCreated_at(),
            transaction.getUser().getId()
        );
    }

    private CompanyTransactionResponseDTO toCompanyResponse(AccountTransactions transaction) {
        return new CompanyTransactionResponseDTO(
            transaction.getId(),
            transaction.getOrigin(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getCreated_at(),
            transaction.getUser().getId(),
            transaction.getAccount().getPeople().getId(),
            transaction.getAccount().getPeople().getName()
        );
    }
}
