package com.mnemos.reward.domain.daily.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class DailyStreakNotRecoverableException :
    AppException(
        "Daily streak is not recoverable",
        "DAILY_STREAK_NOT_RECOVERABLE",
        HttpStatus.CONFLICT,
    )
