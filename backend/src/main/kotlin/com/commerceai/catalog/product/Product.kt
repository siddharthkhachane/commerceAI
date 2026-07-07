package com.commerceai.catalog.product

import com.commerceai.catalog.category.Category
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var slug: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    @Column(nullable = false)
    var imageUrl: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,
    @Column(nullable = false)
    var stockQuantity: Int = 0,
    @Column(nullable = false)
    var isActive: Boolean = true,
    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
)
