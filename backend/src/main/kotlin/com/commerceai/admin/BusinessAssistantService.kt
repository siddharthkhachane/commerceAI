package com.commerceai.admin

import com.commerceai.admin.dto.BusinessAssistantRequest
import com.commerceai.admin.dto.BusinessAssistantResponse
import com.commerceai.admin.dto.BusinessInsight
import com.commerceai.admin.dto.BusinessSnapshot
import com.commerceai.order.Order
import com.commerceai.order.OrderRepository
import com.commerceai.order.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class BusinessAssistantService(
    private val adminDashboardService: AdminDashboardService,
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun analyze(request: BusinessAssistantRequest): BusinessAssistantResponse {
        val question = request.question.trim()
        val metrics = adminDashboardService.getMetrics()
        val analytics = adminDashboardService.getAnalytics(14)

        val now = Instant.now()
        val currentStart = now.minus(7, ChronoUnit.DAYS)
        val previousStart = now.minus(14, ChronoUnit.DAYS)

        val recentOrders = orderRepository.findConfirmedSince(currentStart)
        val previousOrders = orderRepository.findConfirmedBetween(previousStart, currentStart)

        val currentRevenue = sumRevenue(recentOrders)
        val previousRevenue = sumRevenue(previousOrders)
        val revenueChange = percentChange(previousRevenue, currentRevenue)
        val ordersChange = percentChange(
            previousOrders.size.toBigDecimal(),
            recentOrders.size.toBigDecimal(),
        )

        val topProduct = analytics.topProducts.firstOrNull()
        val snapshot = BusinessSnapshot(
            currentPeriodRevenue = currentRevenue,
            previousPeriodRevenue = previousRevenue,
            revenueChangePercent = revenueChange,
            currentPeriodOrders = recentOrders.size.toLong(),
            previousPeriodOrders = previousOrders.size.toLong(),
            ordersChangePercent = ordersChange,
            lowStockProducts = metrics.lowStockProducts,
            activeProducts = metrics.activeProducts,
            totalProducts = metrics.totalProducts,
            topProductName = topProduct?.productName,
            topProductRevenue = topProduct?.revenue,
        )

        val insights = buildInsights(question, snapshot, metrics, analytics.topProducts.size)
        val summary = buildSummary(question, snapshot)
        val recommendations = buildRecommendations(snapshot)

        return BusinessAssistantResponse(
            question = question,
            summary = summary,
            insights = insights,
            snapshot = snapshot,
            recommendations = recommendations,
        )
    }

    private fun sumRevenue(orders: List<Order>): BigDecimal =
        orders.fold(BigDecimal.ZERO) { acc, order -> acc.add(order.totalAmount) }
            .setScale(2, RoundingMode.HALF_UP)

    private fun percentChange(previous: BigDecimal, current: BigDecimal): Double {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return if (current.compareTo(BigDecimal.ZERO) == 0) 0.0 else 100.0
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toDouble()
    }

    private fun buildInsights(
        question: String,
        snapshot: BusinessSnapshot,
        metrics: com.commerceai.admin.dto.AdminMetricsResponse,
        topProductCount: Int,
    ): List<BusinessInsight> {
        val insights = mutableListOf<BusinessInsight>()

        insights += BusinessInsight(
            title = "Revenue trend (last 7 days vs prior 7 days)",
            detail = "Revenue moved from $${snapshot.previousPeriodRevenue} to $${snapshot.currentPeriodRevenue} " +
                "(${formatPercent(snapshot.revenueChangePercent)}).",
            severity = severityForChange(snapshot.revenueChangePercent),
        )

        insights += BusinessInsight(
            title = "Order volume",
            detail = "Orders changed from ${snapshot.previousPeriodOrders} to ${snapshot.currentPeriodOrders} " +
                "(${formatPercent(snapshot.ordersChangePercent)}).",
            severity = severityForChange(snapshot.ordersChangePercent),
        )

        if (snapshot.lowStockProducts > 0) {
            insights += BusinessInsight(
                title = "Inventory pressure",
                detail = "${snapshot.lowStockProducts} products are below the low-stock threshold, " +
                    "which can limit conversions.",
                severity = "warning",
            )
        }

        val inactiveProducts = snapshot.totalProducts - snapshot.activeProducts
        if (inactiveProducts > 0) {
            insights += BusinessInsight(
                title = "Catalog availability",
                detail = "$inactiveProducts products are inactive and hidden from the storefront.",
                severity = "info",
            )
        }

        if (snapshot.topProductName != null && topProductCount > 0) {
            insights += BusinessInsight(
                title = "Top performer",
                detail = "${snapshot.topProductName} leads recent sales at $${snapshot.topProductRevenue}.",
                severity = "success",
            )
        }

        if (question.contains("inventory", ignoreCase = true)) {
            insights += BusinessInsight(
                title = "Inventory units on hand",
                detail = "You currently have ${metrics.inventoryUnits} total inventory units across the catalog.",
                severity = if (snapshot.lowStockProducts > 0) "warning" else "success",
            )
        }

        return insights
    }

    private fun buildSummary(question: String, snapshot: BusinessSnapshot): String {
        val salesIntent = question.contains("sales", ignoreCase = true) ||
            question.contains("revenue", ignoreCase = true) ||
            question.contains("decrease", ignoreCase = true) ||
            question.contains("drop", ignoreCase = true)

        return when {
            salesIntent && snapshot.revenueChangePercent < -5 -> {
                "Sales decreased in the last 7 days compared with the prior week. " +
                    "The decline is likely driven by lower order volume and/or smaller basket sizes. " +
                    "Inventory constraints and catalog availability should be reviewed next."
            }
            salesIntent && snapshot.revenueChangePercent > 5 -> {
                "Sales increased over the last 7 days. Momentum is positive, with stronger revenue and order activity " +
                    "versus the previous week."
            }
            salesIntent -> {
                "Sales are relatively stable week over week. No major revenue swing was detected in the latest period."
            }
            question.contains("inventory", ignoreCase = true) -> {
                "Inventory analysis shows ${snapshot.lowStockProducts} low-stock products out of " +
                    "${snapshot.totalProducts} total SKUs, with ${snapshot.activeProducts} active on the storefront."
            }
            else -> {
                "Business snapshot for the last 7 days: revenue $${snapshot.currentPeriodRevenue}, " +
                    "${snapshot.currentPeriodOrders} orders, and ${snapshot.lowStockProducts} low-stock alerts."
            }
        }
    }

    private fun buildRecommendations(snapshot: BusinessSnapshot): List<String> {
        val recommendations = mutableListOf<String>()

        if (snapshot.revenueChangePercent < -5) {
            recommendations += "Review recent traffic sources and promotions to recover demand."
            recommendations += "Check whether top sellers went out of stock during the period."
        }

        if (snapshot.lowStockProducts > 0) {
            recommendations += "Replenish low-stock products to prevent missed sales."
        }

        if (snapshot.activeProducts < snapshot.totalProducts) {
            recommendations += "Audit inactive products and reactivate high-potential items."
        }

        if (snapshot.topProductName != null) {
            recommendations += "Double down on merchandising for ${snapshot.topProductName} while demand is strong."
        }

        if (recommendations.isEmpty()) {
            recommendations += "Maintain current assortment and monitor weekly revenue and order trends."
        }

        return recommendations.distinct().take(4)
    }

    private fun severityForChange(changePercent: Double): String =
        when {
            changePercent <= -10 -> "warning"
            changePercent >= 10 -> "success"
            else -> "info"
        }

    private fun formatPercent(value: Double): String {
        val prefix = if (value > 0) "+" else ""
        return "$prefix${value.toBigDecimal().setScale(1, RoundingMode.HALF_UP)}%"
    }
}
