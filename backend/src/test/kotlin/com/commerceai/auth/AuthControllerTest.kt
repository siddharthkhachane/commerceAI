package com.commerceai.auth

import com.commerceai.auth.dto.LoginRequest
import com.commerceai.auth.dto.RegisterRequest
import com.commerceai.user.Role
import com.commerceai.user.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAll()
    }

    @Test
    fun `register login and access protected route`() {
        val registerRequest = RegisterRequest(
            fullName = "Jane Customer",
            email = "jane@example.com",
            password = "password123",
        )

        val registerResponse = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { isNotEmpty() }
            jsonPath("$.user.role") { value("CUSTOMER") }
        }.andReturn().response.contentAsString

        val token = objectMapper.readTree(registerResponse).get("token").asText()

        mockMvc.get("/api/auth/me") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("jane@example.com") }
        }

        val loginRequest = LoginRequest(
            email = "jane@example.com",
            password = "password123",
        )

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { isNotEmpty() }
        }
    }

    @Test
    fun `admin route rejects customer token`() {
        val registerRequest = RegisterRequest(
            fullName = "Jane Customer",
            email = "jane@example.com",
            password = "password123",
        )

        val registerResponse = mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerRequest)
        }.andReturn().response.contentAsString

        val token = objectMapper.readTree(registerResponse).get("token").asText()

        mockMvc.get("/api/admin/dashboard") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isForbidden() }
        }
    }
}
