package com.bohdanzhuvak.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank @Email String email,
        @Size(min = 8) String password,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}
