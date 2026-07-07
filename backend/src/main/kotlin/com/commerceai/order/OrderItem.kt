package com.commerceai.order

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

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    @Column(nullable = false)
    var productId: Long,
    @Column(nullable = false)
    var productName: String,
    @Column(nullable = false)
    var productSlug: String,
    @Column(nullable = false)
    var imageUrl: String,
    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,
    @Column(nullable = false)
    var quantity: Int,
)
