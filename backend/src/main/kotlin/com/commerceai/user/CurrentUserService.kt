package com.commerceai.user

import com.commerceai.common.NotFoundException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class CurrentUserService(
    private val userRepository: UserRepository,
) {

    fun requireUser(userDetails: UserDetails): User =
        userRepository.findByEmail(userDetails.username)
            .orElseThrow { NotFoundException("User not found") }
}
