package com.commerceai.catalog.dto

import com.commerceai.catalog.category.Category
import com.commerceai.catalog.product.Product
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class AdminProductResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String,
    val price: BigDecimal,
    val imageUrl: String,
    val stockQuantity: Int,
    val isActive: Boolean,
    val category: CategoryResponse,
)

data class BulkUpdateProductsRequest(
    @field:NotEmpty val productIds: List<Long>,
    val stockQuantity: Int? = null,
    val stockDelta: Int? = null,
    val price: BigDecimal? = null,
    val isActive: Boolean? = null,
)

data class BulkUpdateProductsResponse(
    val updatedCount: Int,
    val products: List<AdminProductResponse>,
)

fun Product.toAdminResponse(): AdminProductResponse =
    AdminProductResponse(
        id = id,
        name = name,
        slug = slug,
        description = description,
        price = price,
        imageUrl = imageUrl,
        stockQuantity = stockQuantity,
        isActive = isActive,
        category = category.toResponse(),
    )
