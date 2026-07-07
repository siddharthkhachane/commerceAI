package com.commerceai.catalog.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySlug(slug: String): Optional<Product>

    fun existsBySlug(slug: String): Boolean

    @Query(
        """
        SELECT p FROM Product p
        JOIN FETCH p.category c
        WHERE p.isActive = true
        AND (:categorySlug IS NULL OR c.slug = :categorySlug)
        AND (
            :search IS NULL OR :search = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """,
    )
    fun findActiveProducts(
        @Param("categorySlug") categorySlug: String?,
        @Param("search") search: String?,
        pageable: Pageable,
    ): Page<Product>

    @Query(
        """
        SELECT p FROM Product p
        JOIN FETCH p.category c
        WHERE p.slug = :slug AND p.isActive = true
        """,
    )
    fun findActiveBySlug(@Param("slug") slug: String): Optional<Product>

    fun countByIsActiveTrue(): Long
}
