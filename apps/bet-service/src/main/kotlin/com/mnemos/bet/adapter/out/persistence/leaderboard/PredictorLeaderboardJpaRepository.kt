package com.mnemos.bet.adapter.out.persistence.leaderboard

import com.mnemos.bet.adapter.out.persistence.eventbet.EventBetEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PredictorLeaderboardJpaRepository : Repository<EventBetEntity, UUID> {
    @Query(
        value = """
            SELECT
                bet.account_id AS "accountId",
                count(*) AS "bets",
                count(*) FILTER (
                    WHERE (round.effective_outcome = 'YES' AND bet.side = 'YES')
                       OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                ) AS "wins",
                sum(
                    CASE
                        WHEN (round.effective_outcome = 'YES' AND bet.side = 'YES')
                          OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                        THEN floor(
                            bet.confirmed_amount *
                            CASE
                                WHEN bet.side = 'YES' THEN round.yes_odds
                                ELSE round.no_odds
                            END
                        )::bigint - bet.confirmed_amount
                        ELSE -bet.confirmed_amount
                    END
                ) AS "profit",
                sum(bet.confirmed_amount) AS "turnover"
            FROM event_bets bet
            JOIN event_bet_rounds round ON round.id = bet.round_id
            WHERE bet.status = 'SETTLED'
              AND bet.confirmed_amount > 0
              AND round.effective_outcome IN ('YES', 'NO')
              AND round.scheduled_at >= :from
              AND (
                  CAST(:to AS timestamptz) IS NULL
                  OR round.scheduled_at < CAST(:to AS timestamptz)
              )
            GROUP BY bet.account_id
            HAVING count(*) >= :minimumSample
            ORDER BY
                CASE WHEN :sort = 'PROFIT' THEN
                    sum(
                        CASE
                            WHEN (round.effective_outcome = 'YES' AND bet.side = 'YES')
                              OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                            THEN floor(
                                bet.confirmed_amount *
                                CASE
                                    WHEN bet.side = 'YES' THEN round.yes_odds
                                    ELSE round.no_odds
                                END
                            )::bigint - bet.confirmed_amount
                            ELSE -bet.confirmed_amount
                        END
                    )
                END DESC,
                CASE WHEN :sort = 'WIN_RATE' THEN
                    count(*) FILTER (
                        WHERE (round.effective_outcome = 'YES' AND bet.side = 'YES')
                           OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                    )::numeric / count(*)
                END DESC,
                CASE WHEN :sort = 'WINS' THEN
                    count(*) FILTER (
                        WHERE (round.effective_outcome = 'YES' AND bet.side = 'YES')
                           OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                    )
                END DESC,
                CASE WHEN :sort = 'TURNOVER' THEN sum(bet.confirmed_amount) END DESC,
                sum(
                    CASE
                        WHEN (round.effective_outcome = 'YES' AND bet.side = 'YES')
                          OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                        THEN floor(
                            bet.confirmed_amount *
                            CASE
                                WHEN bet.side = 'YES' THEN round.yes_odds
                                ELSE round.no_odds
                            END
                        )::bigint - bet.confirmed_amount
                        ELSE -bet.confirmed_amount
                    END
                ) DESC,
                count(*) FILTER (
                    WHERE (round.effective_outcome = 'YES' AND bet.side = 'YES')
                       OR (round.effective_outcome = 'NO' AND bet.side = 'NO')
                ) DESC,
                sum(bet.confirmed_amount) DESC,
                bet.account_id ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun read(
        @Param("from") from: Instant,
        @Param("to") to: Instant?,
        @Param("sort") sort: String,
        @Param("minimumSample") minimumSample: Int,
        @Param("limit") limit: Int,
    ): List<PredictorLeaderboardProjection>
}
