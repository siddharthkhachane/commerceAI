package com.commerceai.catalog.dto

import com.commerceai.catalog.category.Category
import com.commerceai.catalog.product.Product
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val imageUrl: String?,
    val productCount: Long? = null,
)

data class ProductSummaryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val price: BigDecimal,
    val imageUrl: String,
    val category: CategoryResponse,
)

data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String,
    val price: BigDecimal,
    val imageUrl: String,
    val stockQuantity: Int,
    val category: CategoryResponse,
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)

data class CreateCategoryRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 500) val description: String? = null,
    val imageUrl: String? = null,
)

data class UpdateCategoryRequest(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:Size(max = 500) val description: String? = null,
    val imageUrl: String? = null,
)

data class CreateProductRequest(
    @field:NotBlank @field:Size(max = 200) val name: String,
    @field:NotBlank val description: String,
    @field:NotNull @field:DecimalMin("0.01") val price: BigDecimal,
    @field:NotBlank val imageUrl: String,
    @field:NotNull val categoryId: Long,
    @field:Min(0) val stockQuantity: Int = 0,
    val isActive: Boolean = true,
)

data class UpdateProductRequest(
    @field:NotBlank @field:Size(max = 200) val name: String,
    @field:NotBlank val description: String,
    @field:NotNull @field:DecimalMin("0.01") val price: BigDecimal,
    @field:NotBlank val imageUrl: String,
    @field:NotNull val categoryId: Long,
    @field:Min(0) val stockQuantity: Int = 0,
    val isActive: Boolean = true,
)

fun Category.toResponse(productCount: Long? = null): CategoryResponse =
    CategoryResponse(
        id = id,
        name = name,
        slug = slug,
        description = description,
        imageUrl = imageUrl,
        productCount = productCount,
    )

fun Product.toSummaryResponse(): ProductSummaryResponse =
    ProductSummaryResponse(
        id = id,
        name = name,
        slug = slug,
        price = price,
        imageUrl = imageUrl,
        category = category.toResponse(),
    )

fun Product.toDetailResponse(): ProductDetailResponse =
    ProductDetailResponse(
        id = id,
        name = name,
        slug = slug,
        description = description,
        price = price,
        imageUrl = imageUrl,
        stockQuantity = stockQuantity,
        category = category.toResponse(),
    )
