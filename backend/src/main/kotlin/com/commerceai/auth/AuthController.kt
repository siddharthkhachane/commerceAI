package com.commerceai.auth

import com.commerceai.auth.dto.AuthResponse
import com.commerceai.auth.dto.LoginRequest
import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.auth.dto.UserResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): AuthResponse =
        authService.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        authService.login(request)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userDetails: UserDetails): UserResponse =
        authService.getCurrentUser(userDetails.username)
}
