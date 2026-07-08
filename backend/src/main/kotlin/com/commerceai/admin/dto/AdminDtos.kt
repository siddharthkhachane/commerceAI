package com.commerceai.admin.dto

import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.ProductDetailResponse
import com.commerceai.order.OrderStatus
import java.math.BigDecimal
import java.time.Instant

data class AdminMetricsResponse(
    val totalRevenue: BigDecimal,
    val totalOrders: Long,
    val confirmedOrders: Long,
    val totalProducts: Long,
    val activeProducts: Long,
    val lowStockProducts: Long,
    val totalCustomers: Long,
    val inventoryUnits: Long,
)

data class TimeSeriesPoint(
    val date: String,
    val value: BigDecimal,
)

data class OrdersTimeSeriesPoint(
    val date: String,
    val count: Long,
)

data class TopProductStat(
    val productId: Long,
    val productName: String,
    val unitsSold: Long,
    val revenue: BigDecimal,
)

data class CategoryInventoryStat(
    val categoryName: String,
    val productCount: Long,
    val inventoryUnits: Long,
)

data class AdminAnalyticsResponse(
    val revenueSeries: List<TimeSeriesPoint>,
    val ordersSeries: List<OrdersTimeSeriesPoint>,
    val topProducts: List<TopProductStat>,
    val categoryBreakdown: List<CategoryInventoryStat>,
)

data class AdminOrderSummaryResponse(
    val id: Long,
    val customerName: String,
    val customerEmail: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val itemCount: Int,
    val createdAt: Instant,
)

data class AdminInventoryItemResponse(
    val product: ProductDetailResponse,
    val isLowStock: Boolean,
)

data class AdminInventoryResponse(
    val lowStockThreshold: Int,
    val items: PageResponse<AdminInventoryItemResponse>,
)
