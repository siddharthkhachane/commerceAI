package com.commerceai.profile

import com.commerceai.profile.dto.SavedProductResponse
import com.commerceai.profile.dto.UpdateUserProfileRequest
import com.commerceai.profile.dto.UserProfileResponse
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
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService,
    private val currentUserService: CurrentUserService,
) {
    @GetMapping
    fun getProfile(@AuthenticationPrincipal userDetails: UserDetails): UserProfileResponse {
        val user = currentUserService.requireUser(userDetails)
        return profileService.getProfile(user)
    }

    @PutMapping
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UpdateUserProfileRequest,
    ): UserProfileResponse {
        val user = currentUserService.requireUser(userDetails)
        return profileService.updateProfile(user, request)
    }

    @GetMapping("/saved-items")
    fun listSavedItems(@AuthenticationPrincipal userDetails: UserDetails): List<SavedProductResponse> {
        val user = currentUserService.requireUser(userDetails)
        return profileService.listSavedProducts(user)
    }

    @PostMapping("/saved-items/{productId}")
    fun saveItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable productId: Long,
    ): SavedProductResponse {
        val user = currentUserService.requireUser(userDetails)
        return profileService.saveProduct(user, productId)
    }

    @DeleteMapping("/saved-items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeSavedItem(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable productId: Long,
    ) {
        val user = currentUserService.requireUser(userDetails)
        profileService.removeSavedProduct(user, productId)
    }
}
