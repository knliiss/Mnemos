package com.mnemos.reward.domain.daily.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class DailyRewardAlreadyClaimedException :
    AppException(
        "Daily reward already claimed",
        "DAILY_REWARD_ALREADY_CLAIMED",
        HttpStatus.CONFLICT,
    )
