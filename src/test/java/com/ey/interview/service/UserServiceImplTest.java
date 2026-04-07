package com.ey.interview.service;

import com.ey.interview.config.ValidationProperties;
import com.ey.interview.dto.CreateUserRequest;
import com.ey.interview.dto.PhoneRequest;
import com.ey.interview.dto.UserResponse;
import com.ey.interview.entity.User;
import com.ey.interview.exception.EmailAlreadyExistsException;
import com.ey.interview.exception.InvalidEmailException;
import com.ey.interview.exception.InvalidPasswordException;
import com.ey.interview.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private ValidationProperties validationProperties;
    @Mock private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=(.*\\d){2})[a-zA-Z\\d]+$";

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, jwtService, validationProperties, passwordEncoder);
        when(validationProperties.getEmailRegex()).thenReturn(EMAIL_REGEX);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(jwtService, passwordEncoder);
    }

    @Test
    @DisplayName("should register user and return populated response")
    void shouldRegisterUser() {
        stubPasswordValidation();
        var userId = UUID.randomUUID();
        var savedUser = buildUser(userId, "juan@rodriguez.org");

        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UUID.class), anyString())).thenReturn("jwt-token");

        UserResponse response = userService.createUser(buildRequest("juan@rodriguez.org", "Hunter2ab3"));

        assertThat(response.email()).isEqualTo("juan@rodriguez.org");
        assertThat(response.isActive()).isTrue();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.created()).isNotNull();
        verify(userRepository, times(2)).save(any(User.class));
        verify(passwordEncoder).encode(anyString());
        verify(jwtService).generateToken(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("should throw EmailAlreadyExistsException when email is taken")
    void shouldRejectDuplicateEmail() {
        stubPasswordValidation();
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(buildRequest("existing@test.cl", "Hunter2ab3")))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("This email is already used");
    }

    @Test
    @DisplayName("should throw InvalidEmailException when email format is invalid")
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> userService.createUser(buildRequest("not-an-email", "Hunter2ab3")))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("should throw InvalidPasswordException when password has no uppercase")
    void shouldRejectPasswordWithoutUppercase() {
        stubPasswordValidation();

        assertThatThrownBy(() -> userService.createUser(buildRequest("juan@test.cl", "nouppercase22")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("should throw InvalidPasswordException when password has fewer than two digits")
    void shouldRejectPasswordWithInsufficientDigits() {
        stubPasswordValidation();

        assertThatThrownBy(() -> userService.createUser(buildRequest("juan@test.cl", "OnlyOneDigit1")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("should throw InvalidPasswordException when password has no lowercase")
    void shouldRejectPasswordWithoutLowercase() {
        stubPasswordValidation();

        assertThatThrownBy(() -> userService.createUser(buildRequest("juan@test.cl", "ALLUPPERCASE22")))
                .isInstanceOf(InvalidPasswordException.class);
    }

    private void stubPasswordValidation() {
        when(validationProperties.getPasswordRegex()).thenReturn(PASSWORD_REGEX);
    }

    private CreateUserRequest buildRequest(String email, String password) {
        return new CreateUserRequest(
                "Juan Rodriguez",
                email,
                password,
                List.of(new PhoneRequest("1234567", "1", "57"))
        );
    }

    private User buildUser(UUID id, String email) {
        var now = LocalDateTime.now();
        var user = new User();
        user.setId(id);
        user.setName("Juan Rodriguez");
        user.setEmail(email);
        user.setPassword("hashed-password");
        user.setToken("jwt-token");
        user.setActive(true);
        user.setCreated(now);
        user.setModified(now);
        user.setLastLogin(now);
        return user;
    }
}
