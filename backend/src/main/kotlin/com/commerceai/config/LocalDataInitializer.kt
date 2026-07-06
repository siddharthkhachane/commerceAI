package com.commerceai.config

import com.commerceai.user.Role
import com.commerceai.user.User
import com.commerceai.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@Profile("local")
class LocalDataInitializer {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seedAdminUser(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder,
    ): CommandLineRunner = CommandLineRunner {
        val adminEmail = "admin@commerceai.com"
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(
                User(
                    email = adminEmail,
                    passwordHash = passwordEncoder.encode("admin12345"),
                    fullName = "CommerceAI Admin",
                    role = Role.ADMIN,
                ),
            )
            log.info("Seeded admin user: {}", adminEmail)
        }
    }
}
