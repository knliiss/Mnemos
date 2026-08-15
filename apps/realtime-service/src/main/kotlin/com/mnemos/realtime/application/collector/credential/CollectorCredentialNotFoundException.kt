package com.mnemos.realtime.application.collector.credential

import com.mnemos.common.web.exception.AppException
import com.mnemos.realtime.domain.collector.model.CollectorCredentialId
import org.springframework.http.HttpStatus

class CollectorCredentialNotFoundException(
    id: CollectorCredentialId,
) : AppException(
        message = "Collector credential ${id.id} was not found",
        code = "COLLECTOR_CREDENTIAL_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
    )
