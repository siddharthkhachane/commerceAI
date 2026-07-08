package com.commerceai.support

import com.commerceai.assistant.chat.AssistantConversationRepository
import com.commerceai.assistant.chat.AssistantMessageRepository
import com.commerceai.assistant.chat.RecommendationMemoryRepository
import com.commerceai.cart.CartItemRepository
import com.commerceai.catalog.category.CategoryRepository
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.order.OrderRepository
import com.commerceai.profile.SavedProductRepository
import com.commerceai.profile.UserProfileRepository
import com.commerceai.user.UserRepository
import org.springframework.stereotype.Component

@Component
class TestDataCleaner(
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository,
    private val recommendationMemoryRepository: RecommendationMemoryRepository,
    private val assistantMessageRepository: AssistantMessageRepository,
    private val assistantConversationRepository: AssistantConversationRepository,
    private val savedProductRepository: SavedProductRepository,
    private val userProfileRepository: UserProfileRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
) {
    fun cleanAll() {
        orderRepository.deleteAll()
        cartItemRepository.deleteAll()
        recommendationMemoryRepository.deleteAll()
        assistantMessageRepository.deleteAll()
        assistantConversationRepository.deleteAll()
        savedProductRepository.deleteAll()
        userProfileRepository.deleteAll()
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }
}
