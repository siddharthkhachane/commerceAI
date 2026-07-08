package com.commerceai.assistant.chat

import com.commerceai.assistant.chat.dto.ShoppingContextRequest
import com.commerceai.assistant.chat.dto.ShoppingContextSummary
import com.commerceai.cart.CartItemRepository
import com.commerceai.profile.ProfileService
import com.commerceai.profile.SavedProductRepository
import com.commerceai.user.User
import org.springframework.stereotype.Service
import java.math.BigDecimal

data class ResolvedShoppingContext(
    val userId: Long?,
    val cartProductIds: List<Long>,
    val savedProductIds: List<Long>,
    val compareProductIds: List<Long>,
    val viewingProductSlug: String?,
    val stylePreference: String?,
    val preferredCategorySlug: String?,
    val budgetPreference: BigDecimal?,
)

@Service
class ShoppingContextBuilder(
    private val cartItemRepository: CartItemRepository,
    private val savedProductRepository: SavedProductRepository,
    private val profileService: ProfileService,
) {
    fun build(user: User?, request: ShoppingContextRequest): ResolvedShoppingContext {
        val cartIds = if (user != null && request.cartProductIds.isEmpty()) {
            cartItemRepository.findAllByUserId(user.id).map { it.product.id }
        } else {
            request.cartProductIds
        }

        val savedIds = if (user != null && request.savedProductIds.isEmpty()) {
            savedProductRepository.findAllByUserId(user.id).map { it.product.id }
        } else {
            request.savedProductIds
        }

        val profile = user?.let { profileService.getOrCreateProfile(it) }

        return ResolvedShoppingContext(
            userId = user?.id,
            cartProductIds = cartIds.distinct(),
            savedProductIds = savedIds.distinct(),
            compareProductIds = request.compareProductIds.distinct(),
            viewingProductSlug = request.viewingProductSlug?.trim()?.takeIf { it.isNotBlank() },
            stylePreference = profile?.stylePreference,
            preferredCategorySlug = profile?.preferredCategorySlug,
            budgetPreference = profile?.budgetPreference,
        )
    }

    fun toSummary(context: ResolvedShoppingContext): ShoppingContextSummary =
        ShoppingContextSummary(
            cartProductCount = context.cartProductIds.size,
            savedProductCount = context.savedProductIds.size,
            compareProductCount = context.compareProductIds.size,
            viewingProductSlug = context.viewingProductSlug,
            stylePreference = context.stylePreference,
            preferredCategorySlug = context.preferredCategorySlug,
            budgetPreference = context.budgetPreference,
        )
}
