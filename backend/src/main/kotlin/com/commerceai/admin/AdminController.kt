package com.commerceai.admin

import com.commerceai.admin.dto.AdminAnalyticsResponse
import com.commerceai.admin.dto.AdminInventoryResponse
import com.commerceai.admin.dto.AdminMetricsResponse
import com.commerceai.admin.dto.AdminOrderSummaryResponse
import com.commerceai.admin.dto.BusinessAssistantRequest
import com.commerceai.admin.dto.BusinessAssistantResponse
import com.commerceai.catalog.dto.PageResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminDashboardService: AdminDashboardService,
    private val businessAssistantService: BusinessAssistantService,
) {
    @GetMapping("/dashboard")
    fun dashboard(): AdminMetricsResponse = adminDashboardService.getMetrics()

    @GetMapping("/analytics")
    fun analytics(
        @RequestParam(defaultValue = "30") days: Int,
    ): AdminAnalyticsResponse = adminDashboardService.getAnalytics(days)

    @GetMapping("/orders")
    fun orders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PageResponse<AdminOrderSummaryResponse> = adminDashboardService.listOrders(page, size)

    @GetMapping("/inventory")
    fun inventory(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminInventoryResponse = adminDashboardService.listInventory(page, size)

    @PostMapping("/assistant/chat")
    fun businessAssistantChat(
        @Valid @RequestBody request: BusinessAssistantRequest,
    ): BusinessAssistantResponse = businessAssistantService.analyze(request)
}
