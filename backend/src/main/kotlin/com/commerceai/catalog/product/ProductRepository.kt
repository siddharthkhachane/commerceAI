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
        WHERE p.isActive = true
        AND (:categoryFilterOff = true OR c.slug IN :categorySlugs)
        AND (:search IS NULL OR :search = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """,
    )
    fun findActiveProductsByFilters(
        @Param("categoryFilterOff") categoryFilterOff: Boolean,
        @Param("categorySlugs") categorySlugs: List<String>,
        @Param("search") search: String?,
        @Param("maxPrice") maxPrice: java.math.BigDecimal?,
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

    fun countByStockQuantityLessThan(threshold: Int): Long

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p")
    fun sumStockQuantity(): Long?

    @Query(
        """
        SELECT p FROM Product p
        JOIN FETCH p.category c
        ORDER BY p.createdAt DESC
        """,
    )
    fun findAllWithCategory(pageable: Pageable): Page<Product>

    @Query(
        """
        SELECT p FROM Product p
        JOIN FETCH p.category c
        """,
    )
    fun findAllWithCategory(): List<Product>

    @Query(
        """
        SELECT p FROM Product p
        JOIN FETCH p.category c
        WHERE (:search IS NULL OR :search = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:lowStockOnly = false OR p.stockQuantity < :lowStockThreshold)
        AND (:activeStatus IS NULL OR p.isActive = :activeStatus)
        """,
    )
    fun findAdminProducts(
        @Param("search") search: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("lowStockOnly") lowStockOnly: Boolean,
        @Param("lowStockThreshold") lowStockThreshold: Int,
        @Param("activeStatus") activeStatus: Boolean?,
        pageable: Pageable,
    ): Page<Product>
}
