package com.commerceai.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminDashboardResponse(
    val message: String,
)

@RestController
@RequestMapping("/api/admin")
class AdminController {

    @GetMapping("/dashboard")
    fun dashboard(): AdminDashboardResponse =
        AdminDashboardResponse(message = "Welcome to the admin dashboard")
}
