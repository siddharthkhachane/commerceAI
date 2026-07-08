package com.commerceai.profile

import com.commerceai.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "user_profiles")
class UserProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,
    @Column
    var stylePreference: String? = null,
    @Column
    var preferredCategorySlug: String? = null,
    @Column(precision = 10, scale = 2)
    var budgetPreference: BigDecimal? = null,
    @Column(nullable = false)
    var emailNotifications: Boolean = true,
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)
