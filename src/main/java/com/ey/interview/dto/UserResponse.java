package com.ey.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        List<PhoneRequest> phones,
        LocalDateTime created,
        LocalDateTime modified,
        @JsonProperty("last_login") LocalDateTime lastLogin,
        String token,
        @JsonProperty("isactive") boolean isActive
) {}
