package com.store_ops_backend.services;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateEmployeeDTO;
import com.store_ops_backend.models.dtos.EmployeeResponseDTO;
import com.store_ops_backend.models.dtos.TransactionResponseDTO;
import com.store_ops_backend.models.dtos.UpdateEmployeeDTO;
import com.store_ops_backend.models.entities.Account;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.models.entities.UserRole;
import com.store_ops_backend.repositories.AccountTransactionsRepository;
import com.store_ops_backend.repositories.AccountRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.PeopleRepository;
import com.store_ops_backend.repositories.UserRepository;

@Service
public class EmployeeService {

    private static final String DEFAULT_PASSWORD = "storeopsemp01";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserCompanyService userCompanyService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountTransactionsRepository transactionsRepository;

    public EmployeeResponseDTO createEmployee(CreateEmployeeDTO data, String companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        String login = generateUsername(data.name(), company.getName());
        UserRole userRole = parseUserRole(data.role());
        String encryptedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        User user = new User(login, data.name(), encryptedPassword, userRole);
        userRepository.save(user);

        People people = new People(data.name(), "EMPLOYEE", company, user, null, data.contact(), true);
        peopleRepository.save(people);

        Account account = new Account(null, "OPEN", OffsetDateTime.now(), null, people, company);
        accountRepository.save(account);

        String companyRole = "ADMIN".equalsIgnoreCase(data.position()) ? "ADMIN" : "USER";
        UserCompany userCompany = userCompanyService.createUserCompanyWithPosition(
            user,
            company,
            companyRole,
            data.position()
        );

        return toEmployeeResponse(userCompany);
    }

    public List<EmployeeResponseDTO> getAllEmployees(String companyId) {
        List<UserCompany> employees = userCompanyService.getUsersByCompanyId(companyId);
        return employees.stream().map(this::toEmployeeResponse).toList();
    }

    public EmployeeResponseDTO getEmployeeById(String companyId, String userId) {
        UserCompany userCompany = userCompanyService.getUserCompany(companyId, userId);
        return toEmployeeResponse(userCompany);
    }

    @Transactional
    public EmployeeResponseDTO updateEmployee(String companyId, String userId, UpdateEmployeeDTO data) {
        UserCompany userCompany = userCompanyService.getUserCompany(companyId, userId);
        User user = userCompany.getUser();

        if (data.name() != null && !data.name().isBlank()) {
            user.updateName(data.name());
            peopleRepository.findByUserIdAndCompanyId(userId, companyId)
                .ifPresent(people -> people.update(data.name(), null, data.contact(), null));
        } else if (data.contact() != null) {
            peopleRepository.findByUserIdAndCompanyId(userId, companyId)
                .ifPresent(people -> people.update(null, null, data.contact(), null));
        }

        if (data.role() != null && !data.role().isBlank()) {
            user.updateRole(parseUserRole(data.role()));
        }

        userCompany.update(data.role(), data.position(), data.status());
        userCompanyService.saveUserCompany(userCompany);

        return toEmployeeResponse(userCompany);
    }

    @Transactional
    public void resetEmployeePassword(String companyId, String userId) {
        UserCompany userCompany = userCompanyService.getUserCompany(companyId, userId);
        User user = userCompany.getUser();
        user.updatePassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
    }

    public List<TransactionResponseDTO> getEmployeeAccountTransactions(String companyId, String userId) {
        People employeePerson = peopleRepository
            .findByUserIdAndCompanyIdAndType(userId, companyId, "EMPLOYEE")
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        Account account = customerService.findCustomerAccount(companyId, employeePerson.getId());
        return transactionsRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getId())
            .stream()
            .map(transaction -> new TransactionResponseDTO(
                transaction.getId(),
                transaction.getOrigin(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getCreated_at(),
                transaction.getUser().getId()
            ))
            .toList();
    }

    @Transactional
    public void deleteEmployee(String companyId, String userId) {
        userCompanyService.deleteUserCompany(companyId, userId);
    }

    @Transactional
    public void updateEmployeeStatus(String companyId, String userId) {
        userCompanyService.updateUserCompanyStatus(companyId, userId);
    }

    private String generateUsername(String employeeName, String companyName) {
        String namePart = normalizeForUsername(employeeName);
        String companyPart = normalizeForUsername(companyName);
        String base = namePart + "." + companyPart;

        if (userRepository.findBylogin(base) == null) {
            return base;
        }

        int counter = 2;
        while (userRepository.findBylogin(base + counter) != null) {
            counter++;
        }
        return base + counter;
    }

    private String normalizeForUsername(String text) {
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "")
                  .toLowerCase()
                  .replaceAll("[^a-z0-9]", "");
    }

    private UserRole parseUserRole(String role) {
        if (role == null || role.isBlank()) {
            return UserRole.USER;
        }

        String normalized = role.trim().toUpperCase();
        return normalized.equals("ADMIN") ? UserRole.ADMIN : UserRole.USER;
    }

    private EmployeeResponseDTO toEmployeeResponse(UserCompany userCompany) {
        User user = userCompany.getUser();
        String contact = peopleRepository
            .findByUserIdAndCompanyId(user.getId(), userCompany.getCompany().getId())
            .map(People::getContact)
            .orElse(null);
        return new EmployeeResponseDTO(
            user.getId(),
            user.getName(),
            user.getLogin(),
            contact,
            userCompany.getRole(),
            userCompany.getPosition(),
            userCompany.getStatus(),
            userCompany.getJoined_at()
        );
    }
}
