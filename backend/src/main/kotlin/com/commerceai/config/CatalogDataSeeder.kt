package com.commerceai.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class CatalogDataSeeder(
    private val catalogSeedService: CatalogSeedService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun seedCatalog(): CommandLineRunner = CommandLineRunner {
        catalogSeedService.seedIfNeeded()
        log.info("Local catalog seed complete")
    }
}
