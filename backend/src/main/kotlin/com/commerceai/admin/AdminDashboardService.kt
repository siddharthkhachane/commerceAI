package com.commerceai.admin

import com.commerceai.admin.dto.AdminAnalyticsResponse
import com.commerceai.admin.dto.AdminInventoryItemResponse
import com.commerceai.admin.dto.AdminInventoryResponse
import com.commerceai.admin.dto.AdminMetricsResponse
import com.commerceai.admin.dto.AdminOrderSummaryResponse
import com.commerceai.admin.dto.CategoryInventoryStat
import com.commerceai.admin.dto.OrdersTimeSeriesPoint
import com.commerceai.admin.dto.TimeSeriesPoint
import com.commerceai.admin.dto.TopProductStat
import com.commerceai.catalog.dto.PageResponse
import com.commerceai.catalog.dto.toDetailResponse
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.Order
import com.commerceai.order.OrderRepository
import com.commerceai.order.OrderStatus
import com.commerceai.user.Role
import com.commerceai.user.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Service
class AdminDashboardService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
) {
    private val lowStockThreshold = 15

    @Transactional(readOnly = true)
    fun getMetrics(): AdminMetricsResponse {
        val totalRevenue = orderRepository.sumConfirmedRevenue()
        val totalOrders = orderRepository.count()
        val confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED)
        val totalProducts = productRepository.count()
        val activeProducts = productRepository.countByIsActiveTrue()
        val lowStockProducts = productRepository.countByStockQuantityLessThan(lowStockThreshold)
        val totalCustomers = userRepository.countByRole(Role.CUSTOMER)
        val inventoryUnits = productRepository.sumStockQuantity() ?: 0L

        return AdminMetricsResponse(
            totalRevenue = totalRevenue,
            totalOrders = totalOrders,
            confirmedOrders = confirmedOrders,
            totalProducts = totalProducts,
            activeProducts = activeProducts,
            lowStockProducts = lowStockProducts,
            totalCustomers = totalCustomers,
            inventoryUnits = inventoryUnits,
        )
    }

    @Transactional(readOnly = true)
    fun getAnalytics(days: Int): AdminAnalyticsResponse {
        val safeDays = days.coerceIn(7, 90)
        val since = Instant.now().minus(safeDays.toLong(), ChronoUnit.DAYS)
        val confirmedOrders = orderRepository.findConfirmedSince(since)

        val revenueByDate = confirmedOrders.groupBy { it.createdAt.toLocalDate() }
            .mapValues { (_, orders) ->
                orders.fold(BigDecimal.ZERO) { acc, order -> acc.add(order.totalAmount) }
            }

        val ordersByDate = confirmedOrders.groupBy { it.createdAt.toLocalDate() }
            .mapValues { (_, orders) -> orders.size.toLong() }

        val dateRange = (0 until safeDays).map { offset ->
            LocalDate.now(ZoneOffset.UTC).minusDays((safeDays - offset - 1).toLong())
        }

        val revenueSeries = dateRange.map { date ->
            TimeSeriesPoint(
                date = date.toString(),
                value = revenueByDate[date] ?: BigDecimal.ZERO,
            )
        }

        val ordersSeries = dateRange.map { date ->
            OrdersTimeSeriesPoint(
                date = date.toString(),
                count = ordersByDate[date] ?: 0L,
            )
        }

        val topProducts = confirmedOrders
            .flatMap { it.items }
            .groupBy { it.productId }
            .map { (productId, items) ->
                val unitsSold = items.sumOf { it.quantity.toLong() }
                val revenue = items.fold(BigDecimal.ZERO) { acc, item ->
                    acc.add(item.unitPrice.multiply(BigDecimal(item.quantity)))
                }.setScale(2, RoundingMode.HALF_UP)

                TopProductStat(
                    productId = productId,
                    productName = items.first().productName,
                    unitsSold = unitsSold,
                    revenue = revenue,
                )
            }
            .sortedByDescending { it.revenue }
            .take(5)

        val products = productRepository.findAllWithCategory()
        val categoryBreakdown = products
            .groupBy { it.category.name }
            .map { (categoryName, categoryProducts) ->
                CategoryInventoryStat(
                    categoryName = categoryName,
                    productCount = categoryProducts.size.toLong(),
                    inventoryUnits = categoryProducts.sumOf { it.stockQuantity.toLong() },
                )
            }
            .sortedByDescending { it.inventoryUnits }

        return AdminAnalyticsResponse(
            revenueSeries = revenueSeries,
            ordersSeries = ordersSeries,
            topProducts = topProducts,
            categoryBreakdown = categoryBreakdown,
        )
    }

    @Transactional(readOnly = true)
    fun listOrders(page: Int, size: Int): PageResponse<AdminOrderSummaryResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 50), Sort.by("createdAt").descending())
        val result = orderRepository.findAllByOrderByCreatedAtDesc(pageable)

        return PageResponse(
            content = result.content.map { it.toAdminSummary() },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun listInventory(page: Int, size: Int): AdminInventoryResponse {
        val pageable = PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 50),
            Sort.by("stockQuantity").ascending().and(Sort.by("name").ascending()),
        )
        val result = productRepository.findAllWithCategory(pageable)

        return AdminInventoryResponse(
            lowStockThreshold = lowStockThreshold,
            items = PageResponse(
                content = result.content.map {
                    AdminInventoryItemResponse(
                        product = it.toDetailResponse(),
                        isLowStock = it.stockQuantity < lowStockThreshold,
                    )
                },
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
            ),
        )
    }

    private fun Order.toAdminSummary(): AdminOrderSummaryResponse =
        AdminOrderSummaryResponse(
            id = id,
            customerName = user.fullName,
            customerEmail = user.email,
            status = status,
            totalAmount = totalAmount,
            itemCount = items.sumOf { it.quantity },
            createdAt = createdAt,
        )

    private fun Instant.toLocalDate(): LocalDate =
        LocalDate.ofInstant(this, ZoneOffset.UTC)
}
