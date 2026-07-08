package com.commerceai.assistant.dto

import com.commerceai.catalog.dto.ProductSummaryResponse
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class ShoppingAssistantRequest(
    @field:NotBlank val message: String,
    @field:DecimalMin("0.01") val budget: BigDecimal? = null,
)

data class ShoppingAssistantRecommendation(
    val product: ProductSummaryResponse,
    val reason: String,
)

data class ShoppingAssistantResponse(
    val intent: String,
    val budget: BigDecimal?,
    val recommendations: List<ShoppingAssistantRecommendation>,
)
