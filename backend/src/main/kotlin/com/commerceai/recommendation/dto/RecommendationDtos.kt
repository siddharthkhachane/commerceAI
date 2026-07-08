package com.commerceai.recommendation.dto

import com.commerceai.catalog.dto.ProductSummaryResponse

data class ProductRecommendation(
    val product: ProductSummaryResponse,
    val reason: String,
)

data class RecommendationResponse(
    val type: String,
    val recommendations: List<ProductRecommendation>,
)
