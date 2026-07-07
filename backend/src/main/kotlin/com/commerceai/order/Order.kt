package com.commerceai.order

import com.commerceai.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,
    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal,
    @Column(nullable = false)
    var shippingName: String,
    @Column(nullable = false)
    var shippingEmail: String,
    @Column(nullable = false)
    var shippingAddress: String,
    @Column(nullable = false)
    var shippingCity: String,
    @Column(nullable = false)
    var shippingState: String,
    @Column(nullable = false)
    var shippingPostalCode: String,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),
)
