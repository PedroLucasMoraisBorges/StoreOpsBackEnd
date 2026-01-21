package com.store_ops_backend.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateEmployeeDTO;
import com.store_ops_backend.models.dtos.EmployeeResponseDTO;
import com.store_ops_backend.models.dtos.UpdateEmployeeDTO;
import com.store_ops_backend.models.entities.Account;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.People;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.models.entities.UserRole;
import com.store_ops_backend.repositories.AccountRepository;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.PeopleRepository;
import com.store_ops_backend.repositories.UserRepository;

@Service
public class EmployeeService {
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

    public EmployeeResponseDTO createEmployee(CreateEmployeeDTO data, String companyId) {
        if (this.userRepository.findBylogin(data.login()) != null) {
            throw new RuntimeException("Login already exists");
        }

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        String name = data.name() == null || data.name().isBlank() ? data.login() : data.name();
        UserRole userRole = parseUserRole(data.role());
        String encryptedPassword = passwordEncoder.encode(data.password());
        User user = new User(data.login(), name, encryptedPassword, userRole);
        userRepository.save(user);

        People people = new People(name, "EMPLOYEE", company, user);
        peopleRepository.save(people);

        Account account = new Account(null, "OPEN", OffsetDateTime.now(), null, people, company);
        accountRepository.save(account);

        customerService.createCustomerForEmployee(company, user, name);

        String companyRole = data.role() == null || data.role().isBlank() ? "USER" : data.role();
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
                .ifPresent(people -> people.update(data.name(), null, null, null));
        }

        if (data.role() != null && !data.role().isBlank()) {
            user.updateRole(parseUserRole(data.role()));
        }

        userCompany.update(data.role(), data.position(), data.status());
        userCompanyService.saveUserCompany(userCompany);

        return toEmployeeResponse(userCompany);
    }

    @Transactional
    public void deleteEmployee(String companyId, String userId) {
        userCompanyService.deleteUserCompany(companyId, userId);
    }

    @Transactional
    public void updateEmployeeStatus(String companyId, String userId) {
        userCompanyService.updateUserCompanyStatus(companyId, userId);
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
        return new EmployeeResponseDTO(
            user.getId(),
            user.getName(),
            user.getLogin(),
            userCompany.getRole(),
            userCompany.getPosition(),
            userCompany.getStatus(),
            userCompany.getJoined_at()
        );
    }
}
