package com.ey.interview.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotBlank String password,
        @NotNull @Valid List<PhoneRequest> phones
) {}
