package com.commerceai.assistant.dto

import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.ProductSummaryResponse
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class ProductSearchAssistantRequest(
    @field:NotBlank val query: String,
    @field:Min(0) val page: Int = 0,
    @field:Min(1) val size: Int = 24,
    val sort: String? = null,
)

data class ProductSearchFilters(
    val normalizedQuery: String,
    val occasion: String?,
    val categorySlugs: List<String>,
    val keywords: List<String>,
    val maxPrice: BigDecimal?,
)

data class ProductSearchAssistantResponse(
    val filters: ProductSearchFilters,
    val products: PageResponse<ProductSummaryResponse>,
)
