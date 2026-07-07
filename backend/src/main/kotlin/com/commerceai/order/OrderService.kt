package com.commerceai.order

import com.commerceai.cart.CartService
import com.commerceai.common.BadRequestException
import com.commerceai.common.NotFoundException
import com.commerceai.order.dto.CheckoutRequest
import com.commerceai.order.dto.OrderResponse
import com.commerceai.order.dto.toResponse
import com.commerceai.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartService: CartService,
) {

    fun listOrders(user: User): List<OrderResponse> =
        orderRepository.findAllByUserId(user.id).map { it.toResponse() }

    fun getOrder(user: User, orderId: Long): OrderResponse =
        orderRepository.findByIdAndUserId(orderId, user.id)
            .map { it.toResponse() }
            .orElseThrow { NotFoundException("Order not found") }

    @Transactional
    fun checkout(user: User, request: CheckoutRequest): OrderResponse {
        val cartItems = cartService.getCartItems(user)
        if (cartItems.isEmpty()) {
            throw BadRequestException("Your cart is empty")
        }

        cartItems.forEach { item ->
            val product = item.product
            if (!product.isActive) {
                throw BadRequestException("${product.name} is no longer available")
            }
            if (item.quantity > product.stockQuantity) {
                throw BadRequestException("Not enough stock for ${product.name}")
            }
        }

        val totalAmount = cartItems.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.product.price.multiply(BigDecimal(item.quantity)))
        }.setScale(2, RoundingMode.HALF_UP)

        val order = orderRepository.save(
            Order(
                user = user,
                status = OrderStatus.CONFIRMED,
                totalAmount = totalAmount,
                shippingName = request.shippingName.trim(),
                shippingEmail = request.shippingEmail.trim().lowercase(),
                shippingAddress = request.shippingAddress.trim(),
                shippingCity = request.shippingCity.trim(),
                shippingState = request.shippingState.trim(),
                shippingPostalCode = request.shippingPostalCode.trim(),
            ),
        )

        cartItems.forEach { cartItem ->
            val product = cartItem.product
            product.stockQuantity -= cartItem.quantity

            order.items.add(
                OrderItem(
                    order = order,
                    productId = product.id,
                    productName = product.name,
                    productSlug = product.slug,
                    imageUrl = product.imageUrl,
                    unitPrice = product.price,
                    quantity = cartItem.quantity,
                ),
            )
        }

        cartService.clearCart(user)
        return order.toResponse()
    }
}
