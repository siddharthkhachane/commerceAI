package com.commerceai.cart

import com.commerceai.cart.dto.AddCartItemRequest
import com.commerceai.cart.dto.CartResponse
import com.commerceai.cart.dto.UpdateCartItemRequest
import com.commerceai.user.CurrentUserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
    private val currentUserService: CurrentUserService,
) {

    @GetMapping
    fun getCart(@AuthenticationPrincipal userDetails: UserDetails): CartResponse {
        val user = currentUserService.requireUser(userDetails)
        return cartService.getCart(user)
    }

    @PostMapping("/items")
    fun addItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: AddCartItemRequest,
    ): CartResponse {
        val user = currentUserService.requireUser(userDetails)
        return cartService.addItem(user, request)
    }

    @PutMapping("/items/{productId}")
    fun updateItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateCartItemRequest,
    ): CartResponse {
        val user = currentUserService.requireUser(userDetails)
        return cartService.updateItem(user, productId, request)
    }

    @DeleteMapping("/items/{productId}")
    fun removeItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable productId: Long,
    ): CartResponse {
        val user = currentUserService.requireUser(userDetails)
        return cartService.removeItem(user, productId)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun clearCart(@AuthenticationPrincipal userDetails: UserDetails) {
        val user = currentUserService.requireUser(userDetails)
        cartService.clearCart(user)
    }
}
