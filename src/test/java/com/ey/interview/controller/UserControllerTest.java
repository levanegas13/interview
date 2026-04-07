package com.ey.interview.controller;

import com.ey.interview.dto.CreateUserRequest;
import com.ey.interview.dto.PhoneRequest;
import com.ey.interview.dto.UserResponse;
import com.ey.interview.exception.EmailAlreadyExistsException;
import com.ey.interview.exception.InvalidEmailException;
import com.ey.interview.exception.InvalidPasswordException;
import com.ey.interview.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    @DisplayName("should delegate to service and return its response")
    void shouldDelegateToServiceAndReturnResponse() {
        var request = buildRequest("juan@rodriguez.org", "Hunter2ab3");
        var expected = buildResponse("juan@rodriguez.org");

        when(userService.createUser(request)).thenReturn(expected);

        var result = userController.createUser(request);

        assertThat(result).isEqualTo(expected);
        verify(userService).createUser(request);
    }

    @Test
    @DisplayName("should propagate EmailAlreadyExistsException from service")
    void shouldPropagateEmailAlreadyExistsException() {
        var request = buildRequest("existing@test.cl", "Hunter2ab3");

        when(userService.createUser(request)).thenThrow(new EmailAlreadyExistsException());

        assertThatThrownBy(() -> userController.createUser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("This email is already used");
    }

    @Test
    @DisplayName("should propagate InvalidEmailException from service")
    void shouldPropagateInvalidEmailException() {
        var request = buildRequest("not-an-email", "Hunter2ab3");

        when(userService.createUser(request)).thenThrow(new InvalidEmailException());

        assertThatThrownBy(() -> userController.createUser(request))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("should propagate InvalidPasswordException from service")
    void shouldPropagateInvalidPasswordException() {
        var request = buildRequest("juan@test.cl", "weakpassword");

        when(userService.createUser(request)).thenThrow(new InvalidPasswordException());

        assertThatThrownBy(() -> userController.createUser(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    private CreateUserRequest buildRequest(String email, String password) {
        return new CreateUserRequest(
                "Juan Rodriguez",
                email,
                password,
                List.of(new PhoneRequest("1234567", "1", "57"))
        );
    }

    private UserResponse buildResponse(String email) {
        var now = LocalDateTime.now();
        return new UserResponse(
                UUID.randomUUID(), "Juan Rodriguez", email,
                List.of(new PhoneRequest("1234567", "1", "57")),
                now, now, now, "jwt-token", true
        );
    }
}
