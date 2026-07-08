package com.commerceai.profile

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface UserProfileRepository : JpaRepository<UserProfile, Long> {
    fun findByUserId(userId: Long): Optional<UserProfile>
}

interface SavedProductRepository : JpaRepository<SavedProduct, Long> {
    @Query(
        """
        SELECT sp FROM SavedProduct sp
        JOIN FETCH sp.product p
        JOIN FETCH p.category
        WHERE sp.user.id = :userId
        ORDER BY sp.savedAt DESC
        """,
    )
    fun findAllByUserId(@Param("userId") userId: Long): List<SavedProduct>

    fun existsByUserIdAndProductId(userId: Long, productId: Long): Boolean

    fun deleteByUserIdAndProductId(userId: Long, productId: Long)

    fun countByUserId(userId: Long): Long
}
