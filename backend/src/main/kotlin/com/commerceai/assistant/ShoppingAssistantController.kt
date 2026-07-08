package com.commerceai.assistant

import com.commerceai.assistant.dto.ProductCompareAssistantRequest
import com.commerceai.assistant.dto.ProductCompareAssistantResponse
import com.commerceai.assistant.dto.ProductSearchAssistantRequest
import com.commerceai.assistant.dto.ProductSearchAssistantResponse
import com.commerceai.assistant.dto.ShoppingAssistantRequest
import com.commerceai.assistant.dto.ShoppingAssistantResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/assistant")
class ShoppingAssistantController(
    private val shoppingAssistantService: ShoppingAssistantService,
    private val productSearchAssistantService: ProductSearchAssistantService,
    private val productCompareAssistantService: ProductCompareAssistantService,
) {
    @PostMapping("/shopping")
    fun recommendShopping(
        @Valid @RequestBody request: ShoppingAssistantRequest,
    ): ShoppingAssistantResponse = shoppingAssistantService.recommend(request)

    @PostMapping("/product-search")
    fun searchProductsWithAssistant(
        @Valid @RequestBody request: ProductSearchAssistantRequest,
    ): ProductSearchAssistantResponse = productSearchAssistantService.search(request)

    @PostMapping("/compare")
    fun compareProducts(
        @Valid @RequestBody request: ProductCompareAssistantRequest,
    ): ProductCompareAssistantResponse = productCompareAssistantService.compare(request)
}
