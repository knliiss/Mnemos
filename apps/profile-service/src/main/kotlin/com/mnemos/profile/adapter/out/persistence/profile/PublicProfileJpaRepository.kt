package com.mnemos.profile.adapter.out.persistence.profile

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PublicProfileJpaRepository : JpaRepository<PublicProfileEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO public_profiles (
                account_id,
                cristalix_uuid,
                cristalix_nickname,
                likes,
                dislikes,
                comments,
                social_revision,
                created_at,
                updated_at
            )
            VALUES (
                :accountId,
                :cristalixUuid,
                :cristalixNickname,
                0,
                0,
                0,
                0,
                :createdAt,
                :createdAt
            )
            ON CONFLICT (account_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureExists(
        @Param("accountId") accountId: UUID,
        @Param("cristalixUuid") cristalixUuid: UUID,
        @Param("cristalixNickname") cristalixNickname: String,
        @Param("createdAt") createdAt: Instant,
    )

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from PublicProfileEntity profile where profile.accountId = :accountId")
    fun findForUpdate(
        @Param("accountId") accountId: UUID,
    ): PublicProfileEntity?

    @Query(
        value = """
            SELECT p.*
            FROM public_profiles p
            WHERE :query % p.cristalix_nickname
            ORDER BY similarity(:query, p.cristalix_nickname) DESC,
                     p.cristalix_nickname ASC,
                     p.account_id ASC
        """,
        countQuery = """
            SELECT count(*)
            FROM public_profiles p
            WHERE :query % p.cristalix_nickname
        """,
        nativeQuery = true,
    )
    fun searchByNickname(
        @Param("query") query: String,
        pageable: Pageable,
    ): Page<PublicProfileEntity>
}
