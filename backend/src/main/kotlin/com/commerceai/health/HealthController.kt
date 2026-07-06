package com.commerceai.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: Instant,
)

@RestController
class HealthController {

    @GetMapping("/api/health")
    fun health(): HealthResponse =
        HealthResponse(
            status = "UP",
            service = "commerceai-backend",
            timestamp = Instant.now(),
        )
}
