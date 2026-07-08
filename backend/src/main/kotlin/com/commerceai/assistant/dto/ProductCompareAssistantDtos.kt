package com.commerceai.assistant.dto

import com.commerceai.catalog.dto.ProductSummaryResponse
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ProductCompareAssistantRequest(
    @field:NotEmpty
    @field:Size(min = 2, max = 4)
    val productIds: List<Long>,
    val question: String? = null,
)

data class ProductComparisonInsight(
    val product: ProductSummaryResponse,
    val material: String,
    val qualityScore: Int,
    val priceScore: Int,
    val styleScore: Int,
    val pros: List<String>,
    val cons: List<String>,
)

data class ProductCompareAssistantResponse(
    val question: String,
    val comparisons: List<ProductComparisonInsight>,
    val recommendedProductId: Long,
    val recommendation: String,
)
