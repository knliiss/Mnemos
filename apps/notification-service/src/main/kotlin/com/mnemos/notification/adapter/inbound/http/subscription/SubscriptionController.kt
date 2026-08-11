package com.mnemos.notification.adapter.inbound.http.subscription

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.notification.adapter.inbound.http.subscription.SubscriptionApiMapper.toQuery
import com.mnemos.notification.adapter.inbound.http.subscription.SubscriptionApiMapper.toResponse
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionCommand
import com.mnemos.notification.application.subscription.usecase.delete.DeleteSubscriptionUseCase
import com.mnemos.notification.application.subscription.usecase.list.ListSubscriptionsUseCase
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.security.CurrentMnemosUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/subscriptions")
class SubscriptionController(
    private val listSubscriptionsUseCase: ListSubscriptionsUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val currentMnemosUser: CurrentMnemosUser,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListSubscriptionsRequest,
    ): PageResponse<SubscriptionResponse> {
        val userId = UserId(currentMnemosUser.requireUserId())
        return listSubscriptionsUseCase.execute(request.toQuery(userId)).toResponse()
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable subscriptionId: UUID,
    ) {
        deleteSubscriptionUseCase.execute(
            DeleteSubscriptionCommand(
                subscriptionId = SubscriptionId(subscriptionId),
                userId = UserId(currentMnemosUser.requireUserId()),
            ),
        )
    }
}
