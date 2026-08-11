package com.mnemos.reward.domain.daily.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class DailyStreakRestoreLimitReachedException :
    AppException(
        "Monthly daily streak restore limit reached",
        "DAILY_STREAK_RESTORE_LIMIT_REACHED",
        HttpStatus.CONFLICT,
    )
