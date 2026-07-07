package com.commerceai.cart

import com.commerceai.cart.dto.AddCartItemRequest
import com.commerceai.cart.dto.CartResponse
import com.commerceai.cart.dto.UpdateCartItemRequest
import com.commerceai.cart.dto.toResponse
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.common.BadRequestException
import com.commerceai.common.NotFoundException
import com.commerceai.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) {

    fun getCart(user: User): CartResponse =
        buildCartResponse(cartItemRepository.findAllByUserId(user.id))

    @Transactional
    fun addItem(user: User, request: AddCartItemRequest): CartResponse {
        val product = productRepository.findById(request.productId)
            .orElseThrow { NotFoundException("Product not found") }

        if (!product.isActive) {
            throw BadRequestException("Product is not available")
        }

        val existing = cartItemRepository.findByUserIdAndProductId(user.id, product.id)
        val newQuantity = if (existing.isPresent) {
            existing.get().quantity + request.quantity
        } else {
            request.quantity
        }

        if (newQuantity > product.stockQuantity) {
            throw BadRequestException("Not enough stock for ${product.name}")
        }

        if (existing.isPresent) {
            existing.get().quantity = newQuantity
        } else {
            cartItemRepository.save(
                CartItem(
                    user = user,
                    product = product,
                    quantity = newQuantity,
                ),
            )
        }

        return getCart(user)
    }

    @Transactional
    fun updateItem(user: User, productId: Long, request: UpdateCartItemRequest): CartResponse {
        if (request.quantity == 0) {
            cartItemRepository.deleteByUserIdAndProductId(user.id, productId)
            return getCart(user)
        }

        val cartItem = cartItemRepository.findByUserIdAndProductId(user.id, productId)
            .orElseThrow { NotFoundException("Cart item not found") }

        if (request.quantity > cartItem.product.stockQuantity) {
            throw BadRequestException("Not enough stock for ${cartItem.product.name}")
        }

        cartItem.quantity = request.quantity
        return getCart(user)
    }

    @Transactional
    fun removeItem(user: User, productId: Long): CartResponse {
        cartItemRepository.deleteByUserIdAndProductId(user.id, productId)
        return getCart(user)
    }

    @Transactional
    fun clearCart(user: User) {
        cartItemRepository.deleteAllByUserId(user.id)
    }

    fun getCartItems(user: User): List<CartItem> =
        cartItemRepository.findAllByUserId(user.id)

    private fun buildCartResponse(items: List<CartItem>): CartResponse {
        val responses = items.map { it.toResponse() }
        val subtotal = responses.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.lineTotal)
        }.setScale(2, RoundingMode.HALF_UP)

        return CartResponse(
            items = responses,
            itemCount = responses.sumOf { it.quantity },
            subtotal = subtotal,
        )
    }
}
