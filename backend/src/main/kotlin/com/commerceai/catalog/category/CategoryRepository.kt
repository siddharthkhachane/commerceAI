package com.commerceai.catalog.category

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findBySlug(slug: String): Optional<Category>

    fun existsBySlug(slug: String): Boolean
}
