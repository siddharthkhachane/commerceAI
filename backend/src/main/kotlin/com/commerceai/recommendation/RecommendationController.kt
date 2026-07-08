package com.commerceai.recommendation

import com.commerceai.recommendation.dto.RecommendationResponse
import com.commerceai.user.CurrentUserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/recommendations")
class RecommendationController(
    private val recommendationService: RecommendationService,
    private val currentUserService: CurrentUserService,
) {
    @GetMapping("/similar/{slug}")
    fun similarProducts(@PathVariable slug: String): RecommendationResponse =
        recommendationService.similarProducts(slug)

    @GetMapping("/related")
    fun relatedProducts(
        @RequestParam productIds: List<Long>,
    ): RecommendationResponse = recommendationService.relatedProducts(productIds)

    @GetMapping("/for-you")
    fun forYou(
        @AuthenticationPrincipal userDetails: UserDetails,
    ): RecommendationResponse {
        val user = currentUserService.requireUser(userDetails)
        return recommendationService.recommendationsForUser(user.id)
    }
}
