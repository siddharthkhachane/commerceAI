package com.commerceai.admin.dto

import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class BusinessAssistantRequest(
    @field:NotBlank val question: String,
)

data class BusinessInsight(
    val title: String,
    val detail: String,
    val severity: String,
)

data class BusinessSnapshot(
    val currentPeriodRevenue: BigDecimal,
    val previousPeriodRevenue: BigDecimal,
    val revenueChangePercent: Double,
    val currentPeriodOrders: Long,
    val previousPeriodOrders: Long,
    val ordersChangePercent: Double,
    val lowStockProducts: Long,
    val activeProducts: Long,
    val totalProducts: Long,
    val topProductName: String?,
    val topProductRevenue: BigDecimal?,
)

data class BusinessAssistantResponse(
    val question: String,
    val summary: String,
    val insights: List<BusinessInsight>,
    val snapshot: BusinessSnapshot,
    val recommendations: List<String>,
)
