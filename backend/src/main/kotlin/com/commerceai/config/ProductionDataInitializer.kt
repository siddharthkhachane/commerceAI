package com.commerceai.config

import com.commerceai.user.Role
import com.commerceai.user.User
import com.commerceai.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@Profile("prod")
class ProductionDataInitializer(
    private val catalogSeedService: CatalogSeedService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${ADMIN_INITIAL_PASSWORD:admin12345}") private val adminPassword: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seedProductionData(): CommandLineRunner = CommandLineRunner {
        val adminEmail = "admin@commerceai.com"
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(
                User(
                    email = adminEmail,
                    passwordHash = passwordEncoder.encode(adminPassword),
                    fullName = "CommerceAI Admin",
                    role = Role.ADMIN,
                ),
            )
            log.info("Seeded production admin user: {}", adminEmail)
        }

        catalogSeedService.seedIfNeeded()
    }
}
