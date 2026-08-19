package com.mnemos.personalization.application.purchase.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import org.springframework.http.HttpStatus

class PurchaseOperationNotFoundException(
    operationId: PurchaseOperationId,
) : AppException(
    message = "Purchase operation was not found",
    code = "PURCHASE_OPERATION_NOT_FOUND",
    httpStatus = HttpStatus.NOT_FOUND,
    details = mapOf("operationId" to operationId.id),
)
