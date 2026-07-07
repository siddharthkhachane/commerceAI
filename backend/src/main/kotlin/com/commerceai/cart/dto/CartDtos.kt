package com.commerceai.cart.dto

import com.commerceai.cart.CartItem
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.math.RoundingMode

data class AddCartItemRequest(
    @field:NotNull val productId: Long,
    @field:Min(1) val quantity: Int = 1,
)

data class UpdateCartItemRequest(
    @field:Min(0) val quantity: Int,
)

data class CartItemResponse(
    val productId: Long,
    val productName: String,
    val productSlug: String,
    val imageUrl: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
    val stockQuantity: Int,
)

data class CartResponse(
    val items: List<CartItemResponse>,
    val itemCount: Int,
    val subtotal: BigDecimal,
)

fun CartItem.toResponse(): CartItemResponse {
    val lineTotal = unitPrice.multiply(BigDecimal(quantity))
        .setScale(2, RoundingMode.HALF_UP)

    return CartItemResponse(
        productId = product.id,
        productName = product.name,
        productSlug = product.slug,
        imageUrl = product.imageUrl,
        unitPrice = product.price,
        quantity = quantity,
        lineTotal = lineTotal,
        stockQuantity = product.stockQuantity,
    )
}

private val CartItem.unitPrice get() = product.price
