package com.store_ops_backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.store_ops_backend.infra.security.AuthorizationHelper;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.models.entities.UserCompany;
import com.store_ops_backend.models.entities.UserRole;
import com.store_ops_backend.repositories.UserCompanyRepository;

@ExtendWith(MockitoExtension.class)
class AuthorizationIntegrationTest {

    @Mock
    private UserCompanyRepository userCompanyRepository;

    @InjectMocks
    private AuthorizationHelper authorizationHelper;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User("test-user-id", "testuser", "Test User", "hashed-password", UserRole.USER);
    }

    @Test
    void shouldReturn403WhenUserTriesToAccessAnotherCompany() {
        when(userCompanyRepository.findByCompanyIdAndUserId("other-company", "test-user-id"))
            .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
            authorizationHelper.assertUserBelongsToCompany(testUser, "other-company"));
    }

    @Test
    void shouldNotThrowWhenUserAccessesOwnCompany() {
        UserCompany uc = mock(UserCompany.class);
        when(userCompanyRepository.findByCompanyIdAndUserId("my-company", "test-user-id"))
            .thenReturn(Optional.of(uc));

        assertDoesNotThrow(() ->
            authorizationHelper.assertUserBelongsToCompany(testUser, "my-company"));
    }

    @Test
    void shouldReturn403WhenUserHasInsufficientRole() {
        UserCompany uc = mock(UserCompany.class);
        when(uc.getRole()).thenReturn("EMPLOYEE");
        when(userCompanyRepository.findByCompanyIdAndUserId("my-company", "test-user-id"))
            .thenReturn(Optional.of(uc));

        assertThrows(AccessDeniedException.class, () ->
            authorizationHelper.assertUserHasCompanyRole(testUser, "my-company", "OWNER", "MANAGER"));
    }

    @Test
    void shouldReturn403WhenUserHasNoStockAccessToForeignCompany() {
        when(userCompanyRepository.findByCompanyIdAndUserId("other-company", "test-user-id"))
            .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () ->
            authorizationHelper.assertUserHasCompanyRole(testUser, "other-company", "OWNER"));
    }
}
