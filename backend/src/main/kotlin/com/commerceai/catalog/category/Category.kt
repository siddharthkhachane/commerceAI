package com.commerceai.catalog.category

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = false, unique = true)
    var slug: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    var imageUrl: String? = null,
)
