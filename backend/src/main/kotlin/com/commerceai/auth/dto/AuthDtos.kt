package com.commerceai.auth.dto

import com.commerceai.user.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Full name is required")
    @field:Size(max = 100, message = "Full name must be at most 100 characters")
    val fullName: String,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
)

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
)

data class UserResponse(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: Role,
)

data class AuthResponse(
    val token: String,
    val user: UserResponse,
)
