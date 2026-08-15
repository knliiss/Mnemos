package com.mnemos.profile.adapter.out.persistence.profile

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PublicProfileJpaRepository : JpaRepository<PublicProfileEntity, UUID> {
    @Query(
        value = """
            SELECT p.*
            FROM public_profiles p
            WHERE :query <% p.cristalix_nickname
            ORDER BY strict_word_similarity(:query, p.cristalix_nickname) DESC,
                     p.cristalix_nickname ASC
        """,
        countQuery = """
            SELECT count(*)
            FROM public_profiles p
            WHERE :query <% p.cristalix_nickname
        """,
        nativeQuery = true,
    )
    fun searchByNickname(
        @Param("query") query: String,
        pageable: Pageable,
    ): Page<PublicProfileEntity>
}
