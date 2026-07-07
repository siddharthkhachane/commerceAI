package com.commerceai.common

open class ApiException(
    val status: Int,
    override val message: String,
) : RuntimeException(message)

class ConflictException(message: String) : ApiException(409, message)

class UnauthorizedException(message: String) : ApiException(401, message)

class NotFoundException(message: String) : ApiException(404, message)

class BadRequestException(message: String) : ApiException(400, message)

data class ErrorResponse(
    val message: String,
    val errors: Map<String, String>? = null,
)
