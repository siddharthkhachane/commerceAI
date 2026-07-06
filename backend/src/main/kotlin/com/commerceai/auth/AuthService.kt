package com.commerceai.auth

import com.commerceai.auth.dto.AuthResponse
import com.commerceai.auth.dto.LoginRequest
import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.auth.dto.UserResponse
import com.commerceai.common.ConflictException
import com.commerceai.common.UnauthorizedException
import com.commerceai.security.JwtService
import com.commerceai.user.Role
import com.commerceai.user.User
import com.commerceai.user.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("An account with this email already exists")
        }

        val user = userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
                fullName = request.fullName.trim(),
                role = Role.CUSTOMER,
            ),
        )

        return AuthResponse(
            token = jwtService.generateToken(user),
            user = toUserResponse(user),
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim().lowercase()

        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(email, request.password),
            )
        } catch (_: BadCredentialsException) {
            throw UnauthorizedException("Invalid email or password")
        }

        val user = userRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("Invalid email or password") }

        return AuthResponse(
            token = jwtService.generateToken(user),
            user = toUserResponse(user),
        )
    }

    fun getCurrentUser(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UnauthorizedException("User not found") }

        return toUserResponse(user)
    }

    private fun toUserResponse(user: User): UserResponse =
        UserResponse(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            role = user.role,
        )
}
