package com.commerceai.order.dto

import com.commerceai.order.Order
import com.commerceai.order.OrderItem
import com.commerceai.order.OrderStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

data class CheckoutRequest(
    @field:NotBlank val shippingName: String,
    @field:NotBlank @field:Email val shippingEmail: String,
    @field:NotBlank val shippingAddress: String,
    @field:NotBlank val shippingCity: String,
    @field:NotBlank val shippingState: String,
    @field:NotBlank val shippingPostalCode: String,
)

data class OrderItemResponse(
    val productId: Long,
    val productName: String,
    val productSlug: String,
    val imageUrl: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
)

data class OrderResponse(
    val id: Long,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val shippingName: String,
    val shippingEmail: String,
    val shippingAddress: String,
    val shippingCity: String,
    val shippingState: String,
    val shippingPostalCode: String,
    val createdAt: Instant,
    val items: List<OrderItemResponse>,
)

fun Order.toResponse(): OrderResponse =
    OrderResponse(
        id = id,
        status = status,
        totalAmount = totalAmount,
        shippingName = shippingName,
        shippingEmail = shippingEmail,
        shippingAddress = shippingAddress,
        shippingCity = shippingCity,
        shippingState = shippingState,
        shippingPostalCode = shippingPostalCode,
        createdAt = createdAt,
        items = items.map { it.toResponse() },
    )

fun OrderItem.toResponse(): OrderItemResponse {
    val lineTotal = unitPrice.multiply(BigDecimal(quantity))
        .setScale(2, RoundingMode.HALF_UP)

    return OrderItemResponse(
        productId = productId,
        productName = productName,
        productSlug = productSlug,
        imageUrl = imageUrl,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )
}
