package com.commerceai.profile

import com.commerceai.profile.dto.SavedProductResponse
import com.commerceai.profile.dto.UpdateUserProfileRequest
import com.commerceai.profile.dto.UserProfileResponse
import com.commerceai.profile.dto.toPreferencesResponse
import com.commerceai.profile.dto.toResponse
import com.commerceai.catalog.product.ProductRepository
import com.commerceai.common.ConflictException
import com.commerceai.common.NotFoundException
import com.commerceai.order.OrderRepository
import com.commerceai.user.User
import com.commerceai.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val savedProductRepository: SavedProductRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {
    @Transactional(readOnly = true)
    fun getProfile(user: User): UserProfileResponse {
        val profile = getOrCreateProfile(user)
        return buildProfileResponse(user, profile)
    }

    @Transactional
    fun updateProfile(user: User, request: UpdateUserProfileRequest): UserProfileResponse {
        val managedUser = userRepository.findById(user.id).orElseThrow()
        managedUser.fullName = request.fullName.trim()

        val profile = getOrCreateProfile(managedUser)
        profile.stylePreference = request.stylePreference?.trim()?.takeIf { it.isNotBlank() }
        profile.preferredCategorySlug = request.preferredCategorySlug?.trim()?.takeIf { it.isNotBlank() }
        profile.budgetPreference = request.budgetPreference
        profile.emailNotifications = request.emailNotifications
        profile.updatedAt = Instant.now()

        userRepository.save(managedUser)
        userProfileRepository.save(profile)
        return buildProfileResponse(managedUser, profile)
    }

    @Transactional(readOnly = true)
    fun listSavedProducts(user: User): List<SavedProductResponse> =
        savedProductRepository.findAllByUserId(user.id).map { it.toResponse() }

    @Transactional
    fun saveProduct(user: User, productId: Long): SavedProductResponse {
        if (savedProductRepository.existsByUserIdAndProductId(user.id, productId)) {
            throw ConflictException("Product is already saved.")
        }

        val product = productRepository.findById(productId)
            .filter { it.isActive }
            .orElseThrow { NotFoundException("Product not found") }

        val saved = savedProductRepository.save(
            SavedProduct(user = user, product = product),
        )
        return saved.toResponse()
    }

    @Transactional
    fun removeSavedProduct(user: User, productId: Long) {
        if (!savedProductRepository.existsByUserIdAndProductId(user.id, productId)) {
            throw NotFoundException("Saved product not found")
        }
        savedProductRepository.deleteByUserIdAndProductId(user.id, productId)
    }

    @Transactional
    fun getOrCreateProfile(user: User): UserProfile =
        userProfileRepository.findByUserId(user.id).orElseGet {
            userProfileRepository.save(UserProfile(user = user))
        }

    private fun buildProfileResponse(user: User, profile: UserProfile): UserProfileResponse =
        UserProfileResponse(
            id = user.id,
            email = user.email,
            fullName = user.fullName,
            role = user.role,
            preferences = profile.toPreferencesResponse(),
            orderCount = orderRepository.findAllByUserId(user.id).size.toLong(),
            savedItemCount = savedProductRepository.countByUserId(user.id),
        )
}
