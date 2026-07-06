package com.commerceai.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val passwordHash: String,
    @Column(nullable = false)
    val fullName: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
)
