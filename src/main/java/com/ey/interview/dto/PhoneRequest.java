package com.ey.interview.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneRequest(
        @NotBlank String number,
        @NotBlank String citycode,
        @NotBlank String contrycode
) {}
