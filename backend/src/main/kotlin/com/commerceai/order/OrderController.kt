package com.commerceai.order

import com.commerceai.order.dto.CheckoutRequest
import com.commerceai.order.dto.OrderResponse
import com.commerceai.user.CurrentUserService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val currentUserService: CurrentUserService,
) {

    @GetMapping
    fun listOrders(@AuthenticationPrincipal userDetails: UserDetails): List<OrderResponse> {
        val user = currentUserService.requireUser(userDetails)
        return orderService.listOrders(user)
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable orderId: Long,
    ): OrderResponse {
        val user = currentUserService.requireUser(userDetails)
        return orderService.getOrder(user, orderId)
    }

    @PostMapping("/checkout")
    fun checkout(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: CheckoutRequest,
    ): OrderResponse {
        val user = currentUserService.requireUser(userDetails)
        return orderService.checkout(user, request)
    }
}
