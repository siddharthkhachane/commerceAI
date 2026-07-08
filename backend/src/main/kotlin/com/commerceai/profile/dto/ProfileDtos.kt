package com.commerceai.profile.dto

import com.commerceai.catalog.dto.ProductSummaryResponse
import com.commerceai.catalog.dto.toSummaryResponse
import com.commerceai.profile.SavedProduct
import com.commerceai.profile.UserProfile
import com.commerceai.user.Role
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

data class UserPreferencesResponse(
    val stylePreference: String?,
    val preferredCategorySlug: String?,
    val budgetPreference: BigDecimal?,
    val emailNotifications: Boolean,
)

data class UserProfileResponse(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: Role,
    val preferences: UserPreferencesResponse,
    val orderCount: Long,
    val savedItemCount: Long,
)

data class UpdateUserProfileRequest(
    @field:NotBlank @field:Size(max = 100) val fullName: String,
    val stylePreference: String? = null,
    val preferredCategorySlug: String? = null,
    @field:DecimalMin("0.00") val budgetPreference: BigDecimal? = null,
    val emailNotifications: Boolean = true,
)

data class SavedProductResponse(
    val product: ProductSummaryResponse,
    val savedAt: Instant,
)

fun UserProfile.toPreferencesResponse(): UserPreferencesResponse =
    UserPreferencesResponse(
        stylePreference = stylePreference,
        preferredCategorySlug = preferredCategorySlug,
        budgetPreference = budgetPreference,
        emailNotifications = emailNotifications,
    )

fun SavedProduct.toResponse(): SavedProductResponse =
    SavedProductResponse(
        product = product.toSummaryResponse(),
        savedAt = savedAt,
    )
